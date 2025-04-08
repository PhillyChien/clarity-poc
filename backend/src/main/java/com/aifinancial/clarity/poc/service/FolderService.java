package com.aifinancial.clarity.poc.service;

import java.util.List;

import com.aifinancial.clarity.poc.dto.request.FolderRequest;
import com.aifinancial.clarity.poc.dto.response.FolderResponse;

public interface FolderService {
    
    List<FolderResponse> getCurrentUserFolders();
    
    FolderResponse getFolder(Long id);
    
    FolderResponse createFolder(FolderRequest folderRequest);
    
    FolderResponse updateFolder(Long id, FolderRequest folderRequest);
    
    void deleteFolder(Long id);
} 