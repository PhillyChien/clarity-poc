package com.aifinancial.clarity.poc.repository;

import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.Todo;
import com.aifinancial.clarity.poc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    
    List<Todo> findByOwner(User owner);
    
    List<Todo> findByOwnerOrderByCreatedAtDesc(User owner);
    
    List<Todo> findByFolder(Folder folder);
    
    List<Todo> findByFolderOrderByCreatedAtDesc(Folder folder);
    
    List<Todo> findByFolderAndOwner(Folder folder, User owner);
    
    List<Todo> findByFolderAndOwnerOrderByCreatedAtDesc(Folder folder, User owner);
    
    Optional<Todo> findByIdAndOwner(Long id, User owner);
    
    boolean existsByIdAndOwner(Long id, User owner);
} 