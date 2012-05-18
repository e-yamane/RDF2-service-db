/*
 * Copyright (c) 2008-2012
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.framework.service.db;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import jp.rough_diamond.commons.di.DIContainerFactory;
import jp.rough_diamond.framework.service.Service;
import jp.rough_diamond.framework.service.SimpleServiceFinder;

public class ServiceFinder extends SimpleServiceFinder {
	public final static String INTERCEPTOR_KEY = "transactionInterceptor";

	@SuppressWarnings("unchecked")
	public <T extends Service> T getService(Class<T> cl, Class<? extends T> defaultClass) {
		try {
			T base = super.getService(cl, defaultClass);
			ProxyFactory pf = new ProxyFactory(base);
			MethodInterceptor mi = (MethodInterceptor)DIContainerFactory.getDIContainer().getObject(INTERCEPTOR_KEY);
			StaticMethodMatcherPointcutAdvisor interceptor = new ServiceAdvisor();
			interceptor.setAdvice(mi);
			pf.addAdvisor(interceptor);
			pf.setOptimize(true);
			return (T)pf.getProxy();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final static class ServiceAdvisor extends StaticMethodMatcherPointcutAdvisor {
		public final static long serialVersionUID = -1L;
		
		public boolean matches(Method arg0, Class<?> arg1) {
			if(Service.class.isAssignableFrom(arg1) && 
					(arg0.getModifiers() & Modifier.PUBLIC) != 0) {
				return true;
			} else {
				return false;
			}
		}
	}
}
