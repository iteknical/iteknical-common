package com.iteknical.common.beanfinder;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

/**
 * @author wangshenxiang
 */
@Component
public class Finder implements ApplicationContextAware {

    private ApplicationContext applicationContext;


    public Object getBean(Class<?> clazz) {
        return getBean(clazz, Maps.newHashMap());
    }

    public Object getBean(Class<?> clazz, Map<String, Object> props) {
        try {
            Object obj = applicationContext.getBean(clazz);
            for (Entry<String, Object> entry : props.entrySet()) {
                BeanUtils.setProperty(obj, entry.getKey(), entry.getValue());
            }
            return obj;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public Object getBean(String name) {
        return getBean(name, Maps.newHashMap());
    }

    public Object getBean(String name, Map<String, Object> props) {
        try {
            Object obj = applicationContext.getBean(name);
            for (Entry<String, Object> entry : props.entrySet()) {
                BeanUtils.setProperty(obj, entry.getKey(), entry.getValue());
            }
            return obj;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
