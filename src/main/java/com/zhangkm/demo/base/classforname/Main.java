package com.zhangkm.demo.base.classforname;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Main {
	public static void main(String[] args){
        Class<?> nameClass = null;
        try {
            // 根据类名获取Class对象
        	nameClass = Class.forName("com.zhangkm.classforname.NameClass");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // 参数类型数组
            Class<?>[] parameterTypes = { ParameterClass.class, String.class };
            // 根据参数类型获取相应的构造函数
            Constructor<?> constructor = nameClass.getConstructor(parameterTypes);
            // 参数数组
            Object[] parameters = { new ParameterClass(), "Hello World!" };
            // 根据获取的构造函数和参数，创建实例
            Object o = constructor.newInstance(parameters);

            Class<?> methodArgTypes[] = new Class[0];
            Method method = nameClass.getMethod("doSomething", methodArgTypes);
            Object methodArgs[] = new Object[0];
            Object returnObject = method.invoke(o, methodArgs);
            
            if(returnObject!=null && returnObject instanceof ReturnClass){
        		System.out.println("Main: Get Return Object: ReturnClass");
            }else{
        		System.out.println("Main: Error!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

	}
}
