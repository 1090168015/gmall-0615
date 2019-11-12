package com.atguigu.gmall.item.service;

import VO.ItemSaleVO;
import com.atguigu.gmall.core.bean.Resp;
import com.atguigu.gmall.entity.WareSkuEntity;
import com.atguigu.gmall.item.fegin.GmallPmsClient;
import com.atguigu.gmall.item.fegin.GmallSmsClient;
import com.atguigu.gmall.item.fegin.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVO;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.GroupVO;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Service
public class ItemService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    public ItemVO item(Long skuId) {
        ItemVO itemVO = new ItemVO();
        // 1. 查询sku信息
        /*- runAsync方法不支持返回值。
        - supplyAsync可以支持返回值。

 线程串行化方法
thenApply 方法：当一个线程依赖另一个线程时，获取上一个任务返回的结果，并返回当前任务的返回值。
thenAccept方法：消费处理结果。接收任务的处理结果，并消费处理，无返回结果。
thenRun方法：只要上面的任务执行完成，就开始执行thenRun，只是处理完任务后，执行 thenRun的后续操作
       带有Async默认是异步执行的。这里所谓的异步指的是不在当前线程内执行。
        */
        CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            BeanUtils.copyProperties(skuInfoEntity, itemVO);
            return skuInfoEntity;
        }, threadPoolExecutor);


        CompletableFuture<Void> brandCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 2.品牌
            Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandBySpuId(skuInfoEntity.getBrandId());
            itemVO.setBrand(brandEntityResp.getData());
        });


        CompletableFuture<Void> categoryCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 3.分类
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryBySpuId(skuInfoEntity.getCatalogId());
            itemVO.setCategory(categoryEntityResp.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> spuCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 4.spu信息
            Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(skuInfoEntity.getSpuId());
            itemVO.setSpuInfo(spuInfoEntityResp.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> picCompletableFuture = CompletableFuture.runAsync(() -> {
            // 5.设置图片信息
            Resp<List<String>> picsResp = this.gmallPmsClient.queryPicsBySkuId(skuId);
            itemVO.setPic(picsResp.getData());
        }, threadPoolExecutor);

        // 6.营销信息
        CompletableFuture<Void> saleCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<ItemSaleVO>> itemSalveResp = this.gmallSmsClient.queryItemSalveVOs(skuId);
            itemVO.setSales(itemSalveResp.getData());
        }, threadPoolExecutor);


        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            // 7.是否有货
            Resp<List<WareSkuEntity>> wareResp = this.gmallWmsClient.queryWareBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResp.getData();
            itemVO.setStore(wareSkuEntities.stream().anyMatch(t -> t.getStock() > 0));
        }, threadPoolExecutor);

        CompletableFuture<Void> spuSaleCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 8.spu所有的销售属性
            Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsClient.querSaleAttrValues(skuInfoEntity.getSpuId());
            itemVO.setSkuSales(saleAttrValueResp.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> descCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 9.spu的描述信息
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescById(skuInfoEntity.getSpuId());
            itemVO.setDesc(spuInfoDescEntityResp.getData());
        }, threadPoolExecutor);

        CompletableFuture<Void> groupCompletableFuture = skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity -> {
            // 10.规格属性分组及组下的规格参数及值
            Resp<List<GroupVO>> listResp = this.gmallPmsClient.queryGroupVOByCid(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
            itemVO.setGroups(listResp.getData());
        }, threadPoolExecutor);
        CompletableFuture.allOf(skuInfoEntityCompletableFuture,brandCompletableFuture,categoryCompletableFuture,
                                   spuCompletableFuture,picCompletableFuture,saleCompletableFuture,
                                    storeCompletableFuture ,spuSaleCompletableFuture,descCompletableFuture,
                                     groupCompletableFuture );
        return itemVO;

    }
/*没有指定Executor的方法会使用ForkJoinPool.commonPool() 作为它的线程池执行异步代码**。如果指定线程池，则使用指定的线程池运行。*/
    //这里的return是线程执行户的结果集，是线程池代为执行的，执行后可以使用completableFuture方法get（）等方法获取结果集
    public static void main(String[] args) {


        List<CompletableFuture<String>> completableFutureList = Arrays.asList(CompletableFuture.completedFuture("hello"),
        CompletableFuture.completedFuture("world"),
        CompletableFuture.completedFuture("future"));
        CompletableFuture<Void> future = CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[]{}));
        future.whenComplete((t,u)->{
            completableFutureList.stream().forEach(future1->{
                try {
                    System.out.println(future1.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        });
        /*CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("初始化CompletableFuture对象");
     //       int i =1/0;
            return "hello";

        }).thenApply(t->{
            System.out.println("thenApply-----------");
            System.out.println("t-----------"+t);
            return "thenApply";
        }).whenCompleteAsync((t,u)->{
            System.out.println("whenCompleteAsync-----------");
            System.out.println("t-----------"+t);
            System.out.println("u-----------"+u);
        }).exceptionally(t->{
            System.out.println("exceptionally-----------"+t);
            return "exceptionally";
        }).handle((t,u)->{
            System.out.println("handle-----------");
            System.out.println("t-----------"+t);
            System.out.println("u-----------"+u);
            return  "handle";
        }).applyToEither(CompletableFuture.completedFuture("completedFuture"),(t)->{
            System.out.println("thenCombine-----------");
            System.out.println("t-----------"+t);
         //   System.out.println("u-----------"+u);
            return "thenCombine";
        }).handle((t,u)->{
            System.out.println("handle-----------");
            System.out.println("t-----------"+t);
            System.out.println("u-----------"+u);
            return "handle";
        });
*/

       /* try {
            System.out.println(completableFuture.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/


    }
}
/*class MySupplier implements Supplier{
    MySupplier mySupplier = new MySupplier();
    CompletableFuture<String> completableFuturel = CompletableFuture.supplyAsync(mySupplier);
    @Override
    public Object get() {
        System.out.println("初始化CompletableFuture对象");
        return "hello";
    }
}*/

   // public static void main(String[] args) {
            /*new MyThread().start();*/

       /* new Thread(()->{
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
            System.out.println("线程执行");
        }).start();*/
       /* FutureTask<Object> futureTask = new FutureTask<>(() -> {
            TimeUnit.SECONDS.sleep(2);
            System.out.println("处理子进程的业务逻辑");
            return "xxxxxxxxx";
        });
        new Thread(futureTask).start();
        System.out.println(futureTask.get());
        System.out.println("2主线程执行");*/

//        ExecutorService threadPool = Executors.newFixedThreadPool(3);
//        FutureTask<Object> futureTask = new FutureTask<>(() -> {
//            TimeUnit.SECONDS.sleep(2);
//            System.out.println("1处理子进程的业务逻辑");
//            return "xxxxxxxxx";
//        });
//        threadPool.submit(futureTask);
//        System.out.println("2处理主线程的业务逻辑");
//        try {
//            System.out.println("3"+futureTask.get());
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }


   // }

/*class MyThread extends Thread{

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("线程执行");
    }
}*/
/*public ItemVO item(Long skuId) {
         ItemVO itemVO = new ItemVO();
         // 1. 查询sku信息
         Resp<SkuInfoEntity> skuInfoEntityResp = gmallPmsClient.querySkuById(skuId);
         SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
         BeanUtils.copyProperties(skuInfoEntity,itemVO);
         Long spuId = skuInfoEntity.getSpuId();

         // 2.品牌
         Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandBySpuId(skuInfoEntity.getBrandId());
         itemVO.setBrand(brandEntityResp.getData());
         // 3.分类
         Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryBySpuId(skuInfoEntity.getCatalogId());
         itemVO.setCategory(categoryEntityResp.getData());
         // 4.spu信息
         Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(spuId);
         itemVO.setSpuInfo(spuInfoEntityResp.getData());
         // 5.设置图片信息
         Resp<List<String>> picsResp = this.gmallPmsClient.queryPicsBySkuId(skuId);
         itemVO.setPic(picsResp.getData());
         // 6.营销信息
         Resp<List<ItemSaleVO>> itemSalveResp = this.gmallSmsClient.queryItemSalveVOs(skuId);
         itemVO.setSales(itemSalveResp.getData());

         // 7.是否有货
         Resp<List<WareSkuEntity>> wareResp = this.gmallWmsClient.queryWareBySkuId(skuId);
         List<WareSkuEntity> wareSkuEntities = wareResp.getData();
         itemVO.setStore(wareSkuEntities.stream().anyMatch(t ->t.getStock()>0));
         // 8.spu所有的销售属性
         Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsClient.querSaleAttrValues(spuId);
         itemVO.setSkuSales(saleAttrValueResp.getData());
         // 9.spu的描述信息
         Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescById(spuId);
         itemVO.setDesc(spuInfoDescEntityResp.getData());
         // 10.规格属性分组及组下的规格参数及值
         Resp<List<GroupVO>> listResp = this.gmallPmsClient.queryGroupVOByCid(skuInfoEntity.getCatalogId(), spuId);
         itemVO.setGroups(listResp.getData());
         return itemVO;

     }*/
/*
public ItemVO item1(Long skuId) {
    ItemVO itemVO = new ItemVO();
    // 1. 查询sku信息
    */
/*- runAsync方法不支持返回值。
        - supplyAsync可以支持返回值。

 线程串行化方法
thenApply 方法：当一个线程依赖另一个线程时，获取上一个任务返回的结果，并返回当前任务的返回值。
thenAccept方法：消费处理结果。接收任务的处理结果，并消费处理，无返回结果。
thenRun方法：只要上面的任务执行完成，就开始执行thenRun，只是处理完任务后，执行 thenRun的后续操作
       带有Async默认是异步执行的。这里所谓的异步指的是不在当前线程内执行。
        *//*

    CompletableFuture<SkuInfoEntity> skuInfoEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
        Resp<SkuInfoEntity> skuInfoEntityResp = gmallPmsClient.querySkuById(skuId);
        SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
        BeanUtils.copyProperties(skuInfoEntity, itemVO);
        return skuInfoEntity;

    }, threadPoolExecutor);

    // 2.品牌
    skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity->{
        Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandBySpuId(skuInfoEntity.getBrandId());
        itemVO.setBrand(brandEntityResp.getData());
    },threadPoolExecutor);

    // 3.分类
    skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity->{
        Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryBySpuId(skuInfoEntity.getCatalogId());
        itemVO.setCategory(categoryEntityResp.getData());
    },threadPoolExecutor);

    // 4.spu信息
    skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity->{
        Resp<SpuInfoEntity> spuInfoEntityResp = this.gmallPmsClient.querySpuById(skuInfoEntity.getSpuId());
        itemVO.setSpuInfo(spuInfoEntityResp.getData());
    },threadPoolExecutor);

    // 5.设置图片信息
    CompletableFuture.runAsync(()->{
        Resp<List<String>> picsResp = this.gmallPmsClient.queryPicsBySkuId(skuId);
        itemVO.setPic(picsResp.getData());
    },threadPoolExecutor);

    // 6.营销信息
    CompletableFuture.runAsync(()->{
        Resp<List<ItemSaleVO>> itemSalveResp = this.gmallSmsClient.queryItemSalveVOs(skuId);
        itemVO.setSales(itemSalveResp.getData());
    },threadPoolExecutor);


    // 7.是否有货
    CompletableFuture.runAsync(()->{

        Resp<List<WareSkuEntity>> wareResp = this.gmallWmsClient.queryWareBySkuId(skuId);
        List<WareSkuEntity> wareSkuEntities = wareResp.getData();
        itemVO.setStore(wareSkuEntities.stream().anyMatch(t -> t.getStock() > 0));
    },threadPoolExecutor);

    // 8.spu所有的销售属性
    skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity->{
        Resp<List<SkuSaleAttrValueEntity>> saleAttrValueResp = this.gmallPmsClient.querSaleAttrValues(skuInfoEntity.getSpuId());
        itemVO.setSkuSales(saleAttrValueResp.getData());
    },threadPoolExecutor);

    // 9.spu的描述信息
    skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity->{
        Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescById(skuInfoEntity.getSpuId());
        itemVO.setDesc(spuInfoDescEntityResp.getData());
    },threadPoolExecutor);

    // 10.规格属性分组及组下的规格参数及值
    skuInfoEntityCompletableFuture.thenAcceptAsync(skuInfoEntity->{
        Resp<List<GroupVO>> listResp = this.gmallPmsClient.queryGroupVOByCid(skuInfoEntity.getCatalogId(), skuInfoEntity.getSpuId());
        itemVO.setGroups(listResp.getData());
    },threadPoolExecutor);
    CompletableFuture.allOf();
    return itemVO;}
}*/
