package jp.rough_diamond.framework.service.db;

import org.scalatest.Spec
import jp.rough_diamond.framework.service.Service
import jp.rough_diamond.commons.di.AbstractDIContainer
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.aopalliance.intercept.MethodInvocation
import scala.collection.mutable.HashSet
import jp.rough_diamond.commons.di.DIContainerFactory

class ServiceFinderSpec extends Spec { 
    lazy val di = new AbstractDIContainer() {
	  	lazy val map = Map[Object, Object] (
	  		ServiceFinder.INTERCEPTOR_KEY -> interceptor
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
	describe("ServiceFinderの仕様") {
	  it("publicオブジェクトメソッドのみフックする") {
	    classOf[DIContainerFactory].synchronized {
	        DIContainerFactory.setDIContainer(di)
	        interceptor.set = new HashSet
	        val finder = new ServiceFinder
	        val service = finder.getService(classOf[ServiceFinderSpecService], classOf[ServiceFinderSpecService]);
	        service.publicMethod()
	        service.protectedMethod()
	        service.packageMethod()
	        ServiceFinderSpecService.publicStaticMethod
	        ServiceFinderSpecService.protectedStaticMethod()
	        ServiceFinderSpecService.packageStaticMethod()
	        println(interceptor.set)
	        assert(1 == interceptor.set.size)
	        assert(interceptor.set.contains("publicMethod"))
	    }
	  }
	}
	
	object interceptor extends MethodInterceptor {
		var set = new HashSet[String]
	    def invoke(mi:MethodInvocation) : Object = {
		    set += mi.getMethod().getName()
	    	mi.proceed();
	    }
	}
}
