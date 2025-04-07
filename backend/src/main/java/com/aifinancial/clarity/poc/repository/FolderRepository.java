package com.aifinancial.clarity.poc.repository;

import com.aifinancial.clarity.poc.model.Folder;
import com.aifinancial.clarity.poc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    
    List<Folder> findByOwner(User owner);
    
    List<Folder> findByOwnerOrderByCreatedAtDesc(User owner);
    
    Optional<Folder> findByIdAndOwner(Long id, User owner);
    
    boolean existsByIdAndOwner(Long id, User owner);
} 