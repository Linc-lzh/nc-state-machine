package nc.sm.biz.file.service.impl;

import nc.sm.biz.file.pojo.RoutingResult;

public class RoutingService {
    public RoutingResult getRoutingInfo(String fieldId){
        RoutingResult result = new RoutingResult();
        result.setMultiUnit(true);
        return result;
    }
}
