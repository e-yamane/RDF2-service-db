/*
 * Copyright (c) 2008-2012
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.framework.service.db;

import java.sql.Connection;
import java.sql.SQLException;

import org.aopalliance.intercept.MethodInvocation;

import jp.rough_diamond.commons.di.DIContainer;
import jp.rough_diamond.commons.di.DIContainerFactory;

abstract public class ConnectionManager {
	abstract protected boolean isTransactionBegining(MethodInvocation mi);
	abstract public Connection getCurrentConnection(MethodInvocation mi);
	abstract public void beginTransaction(MethodInvocation mi);
	abstract public void rollback(MethodInvocation mi);
	abstract public void commit(MethodInvocation mi) throws SQLException;
	abstract public void clearCache();
	
	public final static String CONNECTION_MANAGER_KEY = "connectionManager";
	
	public static ConnectionManager getConnectionManager() {
		DIContainer container = DIContainerFactory.getDIContainer();
		return (ConnectionManager)container.getObject(CONNECTION_MANAGER_KEY);
	}
}
