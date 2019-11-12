package com.atgui.gmall.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;


@Component
public class AuthGateWayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    @Autowired
    private AuthGateWayFilter gatewayFilter;

    @Override
    public GatewayFilter apply(Object config) {

        return gatewayFilter;
    }
}
