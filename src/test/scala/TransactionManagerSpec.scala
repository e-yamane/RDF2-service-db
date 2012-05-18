package jp.rough_diamond.framework.service.db;

import org.scalatest.Spec
import jp.rough_diamond.commons.di.AbstractDIContainer
import java.sql.Connection
import org.aopalliance.intercept.MethodInvocation
import jp.rough_diamond.commons.di.DIContainerFactory
import jp.rough_diamond.framework.service.Service
import scala.collection.JavaConverters._
import java.util.Stack

class TransactionManagerSpec extends Spec {
    describe("TransactionManagerトランザクションの開始判定") {
        it("NOPアノテーション呼び出しではトランザクションが開始しない事") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.nopFunction
        		assert(values.nopFunctionInTransaction == false)
        	}
        }
        it("REQUIRED_NEWアノテーション呼び出しでトランザクションが開始する事") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.requiredNewFunction
        		assert(values.requiredNewFunctionInTransaction)
        	}
        }
        it("REQUIREDアノテーション呼び出しでトランザクションが開始する事") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.requiredFunction
        		assert(values.requiredFunctionInTransaction)
        	}
        }
        it("アノテーション省略メソッドの呼び出しでトランザクションが開始する事") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.implicitFunction
        		assert(values.implicitFunctionInTransaction)
        	}
        }
    }
    describe("トランザクションの引き継ぎ") {
    	it("REQUIRED_NEW->REQUIREDの呼び出しはトランザクションは継続される。（TransactionContextによって確認する）"){
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.newToNotNew
        		assert(values.contextContinuation)
        	}
    	}
    	it("REQUIRED_NEW->REQUIRED_NEWの呼び出しはトランザクションは分離される。（TransactionContextによって確認する）") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.newToNew
        		assert(!values.contextContinuation)
        	}
    	}
    	it("REQUIRED_NEW->省略の呼び出しはトランザクションは継続される。（TransactionContextによって確認する）") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.newToImplicit
        		assert(values.contextContinuation)
        	}
    	}
    	it("REQUIRED->REQUIRED_NEWの呼び出しはトランザクションは分離される。（TransactionContextによって確認する）"){
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.notNewToNew
        		assert(!values.contextContinuation)
        	}
    	}
    	it("REQUIRED->REQUIREDの呼び出しはトランザクションは継続される。（TransactionContextによって確認する）") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.notNewToNotNew
        		assert(values.contextContinuation)
        	}
    	}
    	it("REQUIRED->省略の呼び出しはトランザクションは継続される。（TransactionContextによって確認する）") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.newToImplicit
        		assert(values.contextContinuation)
        	}
    	}
    	it("省略->REQUIRED_NEWの呼び出しはトランザクションは分離される。（TransactionContextによって確認する）"){
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.implicitToNew
        		assert(!values.contextContinuation)
        	}
    	}
    	it("省略->REQUIREDの呼び出しはトランザクションは継続される。（TransactionContextによって確認する）") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.implicitToNotNew
        		assert(values.contextContinuation)
        	}
    	}
    	it("省略->省略の呼び出しはトランザクションは継続される。（TransactionContextによって確認する）") {
        	classOf[DIContainerFactory].synchronized {
        		DIContainerFactory.setDIContainer(di)
        		val service = TransactionManagerSpecUtils.getService
        		service.implicitToImplicit
        		assert(values.contextContinuation)
        	}
    	}
    }
    
    describe("ロールバックの仕様") {
    	it("正常復帰の場合はロールバックされない事"){ 
        	classOf[DIContainerFactory].synchronized {
        	    cm.isRollback = false;
	    		DIContainerFactory.setDIContainer(di)
	       		val service = TransactionManagerSpecUtils.getService
	       		service.normalReturn;
	    		assert(cm.isRollback == false)
        	}
    	}
    	
    	it("ロールバックオンリーメソッドをサービス内で呼び出すとロールバックされる事") {
        	classOf[DIContainerFactory].synchronized {
        	    cm.isRollback = false;
	    		DIContainerFactory.setDIContainer(di)
	       		val service = TransactionManagerSpecUtils.getService
	       		service.rollbackOnly;
	    		assert(cm.isRollback == true)
        	}
    	}
    	
    	it("異常復帰の場合はロールバックされる事") {
        	classOf[DIContainerFactory].synchronized {
        	    cm.isRollback = false;
	    		DIContainerFactory.setDIContainer(di)
	       		val service = TransactionManagerSpecUtils.getService
	       		try {
	       			service.throwException;
	       			assert(false);
	       		} catch {
	       		  case e :Exception => assert(cm.isRollback == true)
	       		  case x => throw new RuntimeException();
	       		}
        	}
    	}
    }
    describe("コミットの仕様") {
    	it("正常復帰の場合はコミットされる事"){ 
        	classOf[DIContainerFactory].synchronized {
        	    cm.isCommit = false;
	    		DIContainerFactory.setDIContainer(di)
	       		val service = TransactionManagerSpecUtils.getService
	       		service.normalReturn;
	    		assert(cm.isCommit == true)
        	}
    	}
    	
    	it("ロールバックオンリーメソッドをサービス内で呼び出すとコミットされない事") {
        	classOf[DIContainerFactory].synchronized {
        	    cm.isCommit = false;
	    		DIContainerFactory.setDIContainer(di)
	       		val service = TransactionManagerSpecUtils.getService
	       		service.rollbackOnly;
	    		assert(cm.isCommit == false)
        	}
    	}
    	
    	it("異常復帰の場合はコミットされない事") {
        	classOf[DIContainerFactory].synchronized {
        	    cm.isCommit = false;
	    		DIContainerFactory.setDIContainer(di)
	       		val service = TransactionManagerSpecUtils.getService
	       		try {
	       			service.throwException;
	       			assert(false);
	       		} catch {
	       		  case e :Exception => assert(cm.isCommit == false)
	       		  case x => throw new RuntimeException();
	       		}
        	}
    	}
    }
    lazy val di = new AbstractDIContainer() {
	  	lazy val map = Map[Object, Object] (
	  		ServiceFinder.INTERCEPTOR_KEY -> new TransactionManager,
	  		ConnectionManager.CONNECTION_MANAGER_KEY -> cm
	  	)
	    def getObject[T](cl:Class[T], key:Object) : T =  {
	      if(map.get(key) == None) {
	          null.asInstanceOf[T]
	      } else {
	    	  map.get(key).get.asInstanceOf[T];
	      }
	    }
	    def getSource[T](cl:Class[T]) : T = null.asInstanceOf[T]
    };

    object cm extends ConnectionManager {
        var stack = new ThreadLocal[Stack[Int]] {
          override def initialValue() : Stack[Int] = { new Stack[Int] }
        }

        var isRollback = false;
        var isCommit = false;
        
    	def isTransactionBegining(mi:MethodInvocation) : Boolean = { !stack.get().empty() }
    	def getCurrentConnection(mi:MethodInvocation) : Connection = { null }
    	def beginTransaction(mi:MethodInvocation) = { stack.get().push(0) }
    	def rollback(mi:MethodInvocation) = { isRollback = true; stack.get().pop() } 
    	def commit(mi:MethodInvocation) = { isCommit = true; stack.get().pop() }
    	def clearCache() = {}
    }
}

object TransactionManagerSpecUtils {
    def getService : TransactionManagerSpecService = {
		val finder = new ServiceFinder
		finder.getService(classOf[TransactionManagerSpecService], classOf[TransactionManagerSpecService])
    }
}

object values {
	var nopFunctionInTransaction = false
	var requiredNewFunctionInTransaction = false
	var requiredFunctionInTransaction = false
	var implicitFunctionInTransaction = false
	var contextContinuation = false
}

class TransactionManagerSpecService extends Service {
    val TEXT = "TEXT"
	@TransactionAttribute(TransactionAttributeType.NOP)
	def nopFunction = {
	    println("nopFunction")
	    values.nopFunctionInTransaction = TransactionManager.isInTransaction()      
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED_NEW)
	def requiredNewFunction = {
	    println("requiredNewFunction")
	    values.requiredNewFunctionInTransaction = TransactionManager.isInTransaction()      
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def requiredFunction = {
	    println("requiredFunction")
	    values.requiredFunctionInTransaction = TransactionManager.isInTransaction()      
	}

	def implicitFunction = {
	    println("implicitFunction")
	    values.implicitFunctionInTransaction = TransactionManager.isInTransaction()      
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED_NEW)
	def newToNotNew = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallNotNew
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED_NEW)
	def newToNew = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallNew
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED_NEW)
	def newToImplicit = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallImplicit
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def notNewToNotNew = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallNotNew
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def notNewToNew = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallNew
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def notNewToImplicit = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallImplicit
	}

	def implicitToNotNew = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallNotNew
	}

	def implicitToNew = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallNew
	}

	def implicitToImplicit = {
	    getMap.put("abc", "xyz")
	    TransactionManagerSpecUtils.getService.innerCallImplicit
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	def innerCallNotNew = {
	    values.contextContinuation =  getMap.containsKey("abc")
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED_NEW)
	def innerCallNew = {
	    values.contextContinuation =  getMap.containsKey("abc")
	}
	
	def innerCallImplicit = {
	    values.contextContinuation =  getMap.containsKey("abc")
	}
	
	def getMap:java.util.Map[String, String] = {
	  TransactionManager.getTransactionContext().asInstanceOf[java.util.Map[String, String]]
	}
	
	def normalReturn = {}

	def rollbackOnly = {
	    TransactionManager.setRollBackOnly();
	}

	def throwException = {
	    throw new Exception();
	}
}