package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.vo.SkuLockVO;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.core.bean.PageVo;
import com.atguigu.gmall.core.bean.Query;
import com.atguigu.gmall.core.bean.QueryCondition;

import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.entity.WareSkuEntity;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public String checkAndLock(List<SkuLockVO> skuLockVOS) {

        //遍历客户请求锁定数据
        skuLockVOS.forEach(skuLockVO -> {//遍历

            LockSku(skuLockVO);
        });

        //锁定失败，需要回滚
        //查看有没有失败的记录
        //有失败的记录，则回滚成功记录
        List<SkuLockVO> success = skuLockVOS.stream().filter(skuLockVO -> skuLockVO.getLock()).collect(Collectors.toList());
        List<SkuLockVO> error = skuLockVOS.stream().filter(skuLockVO -> !skuLockVO.getLock()).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(error)){//如果有锁定失败的回滚锁定成功的数据
            success.forEach(skuLockVO -> {  //遍历解锁锁定成功的库存，解锁的本质就是更新，再将锁定的值更新回去
                wareSkuDao.unlock(skuLockVO.getSkuWareId(),skuLockVO.getCount());

            });
            return "锁定失败："+error.stream().map(skuLockVO -> skuLockVO.getSkuId()).collect(Collectors.toSet()).toString();
        }
           // return "锁定失败："  +error.stream().map(skuLockVO -> skuLockVO.getSkuId()).collect(Collectors.toSet());
            String orderToken = skuLockVOS.get(0).getOrderToken();
            this.redisTemplate.opsForValue().set("order:stock:"+orderToken, JSON.toJSONString(skuLockVOS));//是由订单号作为redis的键，将锁定的库存数及锁定的库存商品保存到redis中用于后面解锁库存使用
        //，将消息发送到交换机，并使用订单号作为消息内容，发送之后，可以将消息中的订单id取出，然后经订单id作为键，在redis中查询相应锁定库存的信息
            this.amqpTemplate.convertAndSend("WMS-EXCHANGE","wms.unlock",orderToken);//延时队列的routingKey：wms.unlock
            return null;

    }


    private void LockSku(SkuLockVO skuLockVO){

        RLock lock = this.redissonClient.getLock("sku:lock:" + skuLockVO.getSkuId());
        lock.lock();
        List<WareSkuEntity> wareSkuEntities =this.wareSkuDao.checkStore(skuLockVO.getSkuId(),skuLockVO.getCount());

       /* if (CollectionUtils.isEmpty(wareSkuEntities)){//如果为空，但是没有退出会继续往下执行，会报索引越界异常
            skuLockVO.setLock(false);//没有仓库的库存数满足要求，锁定失败
        }
*/
        skuLockVO.setLock(false);//设置初始值，为未锁库存，只有下面条件满足是才锁库存
        if (!CollectionUtils.isEmpty(wareSkuEntities)){
            if (this.wareSkuDao.lock(wareSkuEntities.get(0).getId(), skuLockVO.getCount())==1){
                skuLockVO.setLock(true);//锁库存，设置锁定成功
                skuLockVO.setSkuWareId(wareSkuEntities.get(0).getId());//记录锁定库存的id
            }
        }

        lock.unlock();


    }



}