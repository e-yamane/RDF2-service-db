/*
 * Copyright (c) 2008-2012
 *  Rough Diamond Co., Ltd.              -- http://www.rough-diamond.co.jp/
 *  Information Systems Institute, Ltd.  -- http://www.isken.co.jp/
 *  All rights reserved.
 */
package jp.rough_diamond.framework.service.db;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * トランザクションマネージャ
 */
public class TransactionManager implements MethodInterceptor {
	private final static Log log = LogFactory.getLog(TransactionManager.class);
	
    private static ThreadLocal<Stack<TransactionInterceptor>> transactionBeginingStack = 
    		new ThreadLocal<Stack<TransactionInterceptor>>() {
        protected Stack<TransactionInterceptor> initialValue() {
            return new Stack<TransactionInterceptor>();
        }
    };
    
    private static ThreadLocal<Stack<Map<?, ?>>> 
    	transactionContext = new ThreadLocal<Stack<Map<?, ?>>>() {
    		protected Stack<Map<?, ?>> initialValue() {
    			return new Stack<Map<?, ?>>();
    	}
    };

	/**
	 * 何らかのトランザクション内か否かを返却する 
	 */
	public static boolean isInTransaction() {
		return !transactionContext.get().isEmpty();
	}
	
    /**
     * トランザクションに関連するコンテキストマップを取得する
     */
    public static Map<?, ?> getTransactionContext() {
        Stack<? extends Map<?, ?>> stack2 = transactionContext.get();
        return (stack2.empty()) ? null : stack2.peek();
    }

    /**
	 * トランザクションを開始したInterceptorをスタックに積む
	 * @param ti
	 */
    public static void pushTransactionBeginingInterceptor(TransactionInterceptor ti) {
        Stack<TransactionInterceptor> stack = transactionBeginingStack.get();
        stack.push(ti);
        Stack<Map<?, ?>> stack2 = transactionContext.get();
        Map<?, ?> contextMap = new HashMap<Object, Object>(); 
        stack2.push(contextMap);
    }
    
    /**
     * トランザクションを開始したInterceptorをスタックから除去する
     *
     */
    public static void popTransactionBeginingInterceptor() {
        Stack<TransactionInterceptor> stack = transactionBeginingStack.get();
        stack.pop();
        Stack<? extends Map<?, ?>> stack2 = transactionContext.get();
        stack2.pop();
    }

    /**
     * trueの場合、現在のトランザクションは必ずロールバックされる
     * @return
     */
    public static boolean isRollbackOnly() {
        Stack<TransactionInterceptor> stack = transactionBeginingStack.get();
        TransactionInterceptor ti = (TransactionInterceptor)stack.peek();
        return ti.isRollbackOnly();
    }

    /**
     * 呼び出した時点のトランザクションはロールバックオンリーとなる
     */
    public static void setRollBackOnly() {
        Stack<TransactionInterceptor> stack = transactionBeginingStack.get();
        TransactionInterceptor ti = (TransactionInterceptor)stack.peek();
        ti.setRollbackOnly();
    }

	/**
	 * @param class1
	 */
	public static void addModifiedTemporaryType(Class<? extends Object> cl) {
		Set<Class<?>> set = getModifiedTemporaryTypes();
		set.add(cl);
	}

	@SuppressWarnings("unchecked")
	static Set<Class<?>> getModifiedTemporaryTypes() {
		final String key = TransactionManager.class.getName() + "_temporaryTypes";
		Map<Object, Object> map = (Map<Object, Object>) getTransactionContext();
		Set<Class<?>> ret = (Set<Class<?>>)map.get(key);
		if(ret == null) {
			ret = new HashSet<Class<?>>();
			map.put(key, ret);
		}
		return ret;
	}

	public Object invoke(MethodInvocation mi) throws Throwable {
		log.debug(">>AnnotationTransactionManager#invoke");
		try {
			Method m = mi.getMethod();
			TransactionAttribute ta = m.getAnnotation(TransactionAttribute.class);
			TransactionAttributeType tat = (ta == null) 
					? TransactionAttributeType.REQUIRED : ta.value();
			return tat.doIt(mi);
		} finally {
			log.debug("<<AnnotationTransactionManager#invoke");
		}
	}
}
