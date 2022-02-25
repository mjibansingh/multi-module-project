package com.databasket.auth.config;

import com.databasket.auth.entity.User;

public class SecurityContext {

	private static ThreadLocal<User> currentUser = new ThreadLocal<>();

	public static User getCurrentUser() {
		return currentUser.get();
	}

	public static void setCurrentUser(User user) {
		currentUser.set(user);
	}
	
//	private static ThreadLocal<Instance> currentInstance = new ThreadLocal<>();
//	
//	public static Instance getCurrentInstance() {
//		return currentInstance.get();
//	}
//
//	public static void setCurrentInstance(Instance instance) {
//		currentInstance.set(instance);
//	}
}
