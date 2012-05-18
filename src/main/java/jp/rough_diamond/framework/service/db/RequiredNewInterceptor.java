/*
 * Copyright (c) 2008-2012
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.framework.service.db;

import java.lang.reflect.Modifier;

import org.aopalliance.intercept.MethodInvocation;

public class RequiredNewInterceptor extends TransactionInterceptor {
	@Override
	public Object invoke(MethodInvocation mi) throws Throwable {
		if((mi.getMethod().getModifiers() & Modifier.SYNCHRONIZED) == Modifier.SYNCHRONIZED) {
			return synchronousInvoke(mi);
		} else {
			return unsynchronousInvoke(mi);
		}
	}

	private Object synchronousInvoke(MethodInvocation mi) throws Throwable {
		synchronized(mi.getThis()) {
			return unsynchronousInvoke(mi);
		}
	}
	
	private Object unsynchronousInvoke(MethodInvocation mi) throws Throwable {
		ConnectionManager cm = ConnectionManager.getConnectionManager();
		Throwable ex = null;
		cm.beginTransaction(mi);
        rollbackOnly = Boolean.FALSE;
        TransactionManager.pushTransactionBeginingInterceptor(this);
		Object ret;
		try {
			ret = mi.proceed();
		} catch(Throwable e) {
			ex = e;
			throw e;
		} finally {
			try {
				if(ex == null && !isRollbackOnly()) {
					try {
//FIXME テンポラリーテーブル削除ロジック未定
//						removeTemporary();
						cm.commit(mi);
					} catch(Exception e) {
						cm.rollback(mi);
						throw e;
					}
				} else {
					cm.rollback(mi);
				}
			} finally {
	            TransactionManager.popTransactionBeginingInterceptor();
			}
		}
		return ret;
	}
}
