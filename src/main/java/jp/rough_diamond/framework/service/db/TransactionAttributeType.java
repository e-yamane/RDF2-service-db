/*
 * Copyright (c) 2008-2012
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.framework.service.db;

import org.aopalliance.intercept.MethodInvocation;

public enum TransactionAttributeType {
	REQUIRED() {
		protected TransactionInterceptor getInterceptor() {
			return new RequiredInterceptor();
		}
	}, 
	REQUIRED_NEW(){
		protected TransactionInterceptor getInterceptor() {
			return new RequiredNewInterceptor();
		}
	},
	NOP(){
		protected TransactionInterceptor getInterceptor() {
			return new NopInterceptor();
		}
	};

	public Object doIt(MethodInvocation mi) throws Exception {
		try {
			return getInterceptor().invoke(mi);
		} catch (Throwable e) {
			if(e instanceof Error) {
				throw (Error)e;
			} else {
				throw (Exception)e;
			}
		}
	}
	
	abstract protected TransactionInterceptor getInterceptor();
}
