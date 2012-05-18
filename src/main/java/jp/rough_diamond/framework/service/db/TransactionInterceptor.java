/*
 * Copyright (c) 2008-2012
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.framework.service.db;

import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract public class TransactionInterceptor implements MethodInterceptor {
    private final static Log log = LogFactory.getLog(TransactionInterceptor.class);

    protected boolean rollbackOnly = Boolean.FALSE;

    public boolean isRollbackOnly() {
        return rollbackOnly; 
    }

	public void setRollbackOnly() {
        log.debug("Call RollbackOnly!");
        rollbackOnly = Boolean.TRUE;
    }

// FIXME テンポラリーテーブル削除ロジックの再検討	
//	protected void removeTemporary() {
//		Set<Class<?>> types = shapeUp(TransactionManager.getModifiedTemporaryTypes());
//		for(Class<?> type : types) {
//			BasicService.getService().deleteAll(type);
//		}
//	}

//	static Set<Class<?>> shapeUp(Set<Class<?>> set) {
//		//XXX ちょいとダサいがまぁそんなにテンポラリーが多いとは思わないし良いか？
//		Set<Class<?>> tmp = new HashSet<Class<?>>(set);
//		for(Class<?> cl : set) {
//			Class<?> tmpCl = cl.getSuperclass();
//			tmpCl = (tmpCl == null) ? Object.class : tmpCl;
//			while(tmpCl != Object.class) {
//				if(tmp.contains(tmpCl)) {
//					tmp.remove(cl);
//					break;
//				}
//				tmpCl = tmpCl.getSuperclass();
//			}
//		}
//		return tmp;
//	}
}
