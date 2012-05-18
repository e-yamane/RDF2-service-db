/*
 * Copyright (c) 2008-2012
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.framework.service.db;

import org.aopalliance.intercept.MethodInvocation;

public class NopInterceptor extends TransactionInterceptor {

	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		return mi.proceed();
	}
}
