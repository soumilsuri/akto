package com.akto.action;


import com.akto.dao.ApiCollectionsDao;
import com.akto.dao.ApiInfoDao;
import com.akto.dao.SingleTypeInfoDao;
import com.akto.dto.ApiCollection;
import com.akto.dto.ApiInfo;
import com.akto.dto.ApiInfo.ApiInfoKey;
import com.akto.dto.type.SingleTypeInfo;
import com.akto.usage.UsageMetricCalculator;
import com.akto.util.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;

public class ApiInfoAction extends UserAction {
    @Override
    public String execute() {
        return SUCCESS;
    }

    private List<ApiInfo> apiInfoList;
    private int apiCollectionId;
    public String fetchApiInfoList() {
        apiInfoList= ApiInfoDao.instance.findAll(Filters.eq("_id.apiCollectionId", apiCollectionId));
        for (ApiInfo apiInfo: apiInfoList) {
            apiInfo.calculateActualAuth();
        }
        return SUCCESS.toUpperCase();
    }

    private String url ;
    private String method;
    private ApiInfo apiInfo;

    public String fetchApiInfo(){
        Bson filter = ApiInfoDao.getFilter(url, method, apiCollectionId);
        this.apiInfo = ApiInfoDao.instance.findOne(filter);
        if(this.apiInfo == null){
            // case of slash missing in first character of url
            // search for url having no leading slash
            if (url != null && url.startsWith("/")) {
                String urlWithoutLeadingSlash = url.substring(1);
                filter = ApiInfoDao.getFilter(urlWithoutLeadingSlash, method, apiCollectionId);
                this.apiInfo = ApiInfoDao.instance.findOne(filter);   
            }
        }
        return SUCCESS.toUpperCase();
    }

    public List<ApiInfo> getApiInfoList() {
        return apiInfoList;
    }

    public void setApiCollectionId(int apiCollectionId) {
        this.apiCollectionId = apiCollectionId;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public ApiInfo getApiInfo() {
        return apiInfo;
    }

    private String type;
    private int lowerLimitValue;
    private int higherLimitValue;
    private String fieldName;
    private List<ApiInfo> apiInfos;

    public String fetchApiInfosWithCustomFilter() {
        switch (type) {
            case "RISK_SCORE":
                handleRiskScoreFilter();
                break;
            case "SENSITIVE":
                handleSensitiveFilter();
                break;
            case "AUTH_TYPES":
                handleAuthTypesFilter();
                break;
            case "THIRD_PARTY":
                handleThirdPartyFilter();
                break;
            default:
                addActionError("Invalid filter type: " + type);
                return ERROR.toUpperCase();
        }
        return SUCCESS.toUpperCase();
    }

    

    private void handleRiskScoreFilter() {
        Bson filter = Filters.gt(fieldName, lowerLimitValue);
        apiInfos = ApiInfoDao.instance.findAll(filter);
    }


    // private void handleSensitiveFilter() {
    //     Bson filter = Filters.eq(ApiInfo.IS_SENSITIVE, true);
    //     apiInfos = ApiInfoDao.instance.findAll(filter);
    // }

    private void handleSensitiveFilter() {
        List<String> sensitiveSubtypes = SingleTypeInfoDao.instance.sensitiveSubTypeInResponseNames();
        sensitiveSubtypes.addAll(SingleTypeInfoDao.instance.sensitiveSubTypeNames());
        sensitiveSubtypes.addAll(SingleTypeInfoDao.instance.sensitiveSubTypeInRequestNames());

        BasicDBObject groupedId = new BasicDBObject(SingleTypeInfo._API_COLLECTION_ID, "$apiCollectionId")
                                    .append(SingleTypeInfo._URL, "$url")
                                    .append(SingleTypeInfo._METHOD, "$method");

        Bson customFilter = Filters.nin(SingleTypeInfo._API_COLLECTION_ID, UsageMetricCalculator.getDeactivated());

        List<Bson> pipeline = SingleTypeInfoDao.instance.generateFilterForSubtypes(sensitiveSubtypes, groupedId, false, customFilter);

        List<Bson> orConditions = new ArrayList<>();

        try (MongoCursor<BasicDBObject> cursor = SingleTypeInfoDao.instance.getMCollection().aggregate(pipeline, BasicDBObject.class).cursor()) {
            while (cursor.hasNext()) {
                BasicDBObject doc = cursor.next();
                BasicDBObject id = (BasicDBObject) doc.get(Constants.ID);

                Integer collectionId = id.getInt(SingleTypeInfo._API_COLLECTION_ID);
                String url = id.getString(SingleTypeInfo._URL);
                String method = id.getString(SingleTypeInfo._METHOD);

                Bson andClause = Filters.and(
                    Filters.eq(ApiInfoKey.API_COLLECTION_ID, collectionId),
                    Filters.eq(ApiInfoKey.URL, url),
                    Filters.eq(ApiInfoKey.METHOD, method)
                );
                orConditions.add(andClause);
            }
        }

        if (!orConditions.isEmpty()) {
            Bson finalFilter = Filters.or(orConditions);
            apiInfos = ApiInfoDao.instance.findAll(finalFilter);
        } else {
            apiInfos = new ArrayList<>();
        }
    }



    private void handleAuthTypesFilter() {
        List<ApiInfo> allApis = ApiInfoDao.instance.findAll(new BsonDocument());
        List<ApiInfo> unauthenticatedApis = new ArrayList<>();
        
        for (ApiInfo apiInfo : allApis) {
            apiInfo.calculateActualAuth();
            List<ApiInfo.AuthType> actualAuthTypes = apiInfo.getActualAuthType();
            if (actualAuthTypes != null && !actualAuthTypes.isEmpty() && actualAuthTypes.get(0) == ApiInfo.AuthType.UNAUTHENTICATED) {
                unauthenticatedApis.add(apiInfo);
            }
        }
        apiInfos = unauthenticatedApis;
    }

    private void handleThirdPartyFilter() {
        int sevenDaysAgo = (int) (System.currentTimeMillis() / 1000) - 604800; // 7 days in seconds
        Bson filter = Filters.and(
            Filters.gte(fieldName, sevenDaysAgo),
            Filters.in("apiAccessTypes", "THIRD_PARTY")
        );
        apiInfos = ApiInfoDao.instance.findAll(filter);
    }

    // Getters and setters
    public List<ApiInfo> getApiInfos() {
        return apiInfos;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLowerLimitValue(int lowerLimitValue) {
        this.lowerLimitValue = lowerLimitValue;
    }

    public void setHigherLimitValue(int higherLimitValue) {
        this.higherLimitValue = higherLimitValue;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

}
