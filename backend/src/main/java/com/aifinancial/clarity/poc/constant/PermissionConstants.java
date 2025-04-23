package com.aifinancial.clarity.poc.constant;

/**
 * PermissionConstants defines all permission identifiers in the system
 */
public class PermissionConstants {
    // Todos permissions - own todos
    public static final String TODOS_OWN_VIEW = "todos.own.view";
    public static final String TODOS_OWN_CREATE = "todos.own.create";
    public static final String TODOS_OWN_EDIT = "todos.own.edit";
    public static final String TODOS_OWN_DELETE = "todos.own.delete";
    
    // Todos permissions - others' todos
    public static final String TODOS_OTHERS_VIEW = "todos.others.view";
    public static final String TODOS_OTHERS_BAN = "todos.others.ban";
    
    // Folders permissions - own folders
    public static final String FOLDERS_OWN_VIEW = "folders.own.view";
    public static final String FOLDERS_OWN_CREATE = "folders.own.create";
    public static final String FOLDERS_OWN_EDIT = "folders.own.edit";
    public static final String FOLDERS_OWN_DELETE = "folders.own.delete";
    
    // Folders permissions - others' folders
    public static final String FOLDERS_OTHERS_VIEW = "folders.others.view";
    
    // Users permissions - view
    public static final String USERS_VIEW = "users.view";
    // Users permissions - manage
    public static final String USERS_MANAGE = "users.manage";
    
    private PermissionConstants() {}
} 