package com.iteknical.common.utils;

import com.iteknical.common.entity.ResultDTO;

/**
 * @author Tony
 */
public class ResultDTOUtils {
    public <T> T checkResultAndGetData(ResultDTO<T> resultDTO) {
        if (resultDTO.isSuccess() == false) {
            throw new RuntimeException("code=" + resultDTO.getCode() + ", message=" + resultDTO.getMessage());
        }
        return resultDTO.getData();
    }
}
