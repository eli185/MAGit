package logic;

import jaxb.schema.generated.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

public class ReadFromXML {
    private final static String JAXB_XML_MAGIT_PACKAGE_NAME = "jaxb.schema.generated";
    private MagitRepository m_MagitRepository;

    public ReadFromXML(String i_XMLPath) throws JAXBException, FileNotFoundException {
        m_MagitRepository = deserializeFromXML(i_XMLPath);
    }

    public MagitRepository getMagitRepository() {
        return m_MagitRepository;
    }

    private static MagitRepository deserializeFromXML(String i_XMLPath) throws JAXBException, FileNotFoundException {
        InputStream inputStream = new FileInputStream(i_XMLPath);
        MagitRepository magitRepository = deserializeFrom(inputStream);

        return magitRepository;
    }

    private static MagitRepository deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(JAXB_XML_MAGIT_PACKAGE_NAME);
        Unmarshaller u = jc.createUnmarshaller();

        return (MagitRepository) u.unmarshal(in);
    }

   public boolean isXMLValid(List<String> i_Errors) throws JAXBException {
        return (checkUniquenessOfAllIdsInRepository(i_Errors) && checkExistenceOfAllBlobsInFolders(m_MagitRepository.getMagitFolders().getMagitSingleFolder(), m_MagitRepository.getMagitBlobs().getMagitBlob(), i_Errors)
                && checkExistenceOfAllFolderInFolders(m_MagitRepository.getMagitFolders().getMagitSingleFolder(), i_Errors) && checkIfFolderIdIsInInnerFolders(m_MagitRepository.getMagitFolders().getMagitSingleFolder(), i_Errors)
                && checkExistenceOfReferencesFromCommitsToFolders(m_MagitRepository.getMagitCommits().getMagitSingleCommit(), m_MagitRepository.getMagitFolders().getMagitSingleFolder(), i_Errors) && checkReferencesFromCommitsToFoldersValidity(m_MagitRepository.getMagitCommits().getMagitSingleCommit(), m_MagitRepository.getMagitFolders().getMagitSingleFolder(), i_Errors)
                && checkBranchesValidity(m_MagitRepository.getMagitBranches().getMagitSingleBranch(), m_MagitRepository.getMagitCommits().getMagitSingleCommit(), i_Errors) && isHeadValid(m_MagitRepository.getMagitBranches(), i_Errors)) && isRemoteValid(i_Errors)
                && isTrackingAfterValid(m_MagitRepository.getMagitBranches().getMagitSingleBranch(), i_Errors);
    }

    // check 3.2
    private boolean checkUniquenessOfAllIdsInRepository(List<String> i_Errors) {
        boolean checkUniquenessOfAllIdsInRepository = (checkUniquenessOfAllIdsInBlobs(m_MagitRepository.getMagitBlobs().getMagitBlob()) && checkUniquenessOfAllIdsInFolders(m_MagitRepository.getMagitFolders().getMagitSingleFolder())
                && checkUniquenessOfAllIdsInCommits(m_MagitRepository.getMagitCommits().getMagitSingleCommit()));

        if(!checkUniquenessOfAllIdsInRepository){
            i_Errors.add("There are two elements with the same id");
        }

        return checkUniquenessOfAllIdsInRepository;
    }

    private boolean checkUniquenessOfAllIdsInBlobs(List<MagitBlob> i_Blobs) {
        Set<String> setOfIds = new HashSet<>();
        boolean checkUniquenessOfAllIdsInBlobs = true;

        for (MagitBlob blob : i_Blobs) {
            if (!setOfIds.add(blob.getId())) {
                checkUniquenessOfAllIdsInBlobs = false;
                break;
            }
        }

        return checkUniquenessOfAllIdsInBlobs;
    }

    private boolean checkUniquenessOfAllIdsInFolders(List<MagitSingleFolder> i_Folders) {
        Set<String> setOfIds = new HashSet<>();
        boolean checkUniquenessOfAllIdsInFolders = true;

        for (MagitSingleFolder folder : i_Folders) {
            if (!setOfIds.add(folder.getId())) {
                checkUniquenessOfAllIdsInFolders = false;
                break;
            }
        }

        return checkUniquenessOfAllIdsInFolders;
    }

    private boolean checkUniquenessOfAllIdsInCommits(List<MagitSingleCommit> i_Commits) {
        Set<String> setOfIds = new HashSet<>();
        boolean checkUniquenessOfAllIdsInCommits = true;

        for (MagitSingleCommit commit : i_Commits) {
            if (!setOfIds.add(commit.getId())) {
                checkUniquenessOfAllIdsInCommits = false;
                break;
            }
        }

        return checkUniquenessOfAllIdsInCommits;
    }

    // check 3.3
    private boolean checkExistenceOfAllBlobsInFolders(List<MagitSingleFolder> i_Folders, List<MagitBlob> i_Blobs, List<String> i_Errors) {
        Set<String> setOfBlobsIds = new HashSet<>();
        Set<String> setOfBlobsIdsInSingleFolders = new HashSet<>();
        boolean checkExistenceOfAllBlobsInFolders = false;

        for (MagitBlob blob : i_Blobs) {
            setOfBlobsIds.add(blob.getId());
        }

        for(MagitSingleFolder folder: i_Folders) {
            getAllBlobsIdsInSingleFoldersRec(i_Folders, folder, setOfBlobsIdsInSingleFolders);
        }

        if(setOfBlobsIds.size() == setOfBlobsIdsInSingleFolders.size()) {
            setOfBlobsIds.removeAll(setOfBlobsIdsInSingleFolders);
            if(setOfBlobsIds.size() == 0) {
                checkExistenceOfAllBlobsInFolders = true;
            }
        }

        if(!checkExistenceOfAllBlobsInFolders){
            i_Errors.add("There is a reference from a folder to blob that does not exist");
        }

        return checkExistenceOfAllBlobsInFolders;
    }

    private void getAllBlobsIdsInSingleFoldersRec(List<MagitSingleFolder> i_Folders, MagitSingleFolder i_Folder, Set<String> i_SetOfBlobsIdsInSingleFolders){
        List<Item> items = i_Folder.getItems().getItem();
        MagitSingleFolder nextFolder = null;

        for (Item item : items) {
            if (item.getType().equals("blob")) {
                i_SetOfBlobsIdsInSingleFolders.add(item.getId());
            }
            else {
                for (MagitSingleFolder folder : i_Folders) {
                    if (folder.getId().equals(item.getId())) {
                        nextFolder = folder;
                        break;
                    }
                }
                getAllBlobsIdsInSingleFoldersRec(i_Folders, nextFolder, i_SetOfBlobsIdsInSingleFolders);
            }
        }
    }

    // check 3.4
    private boolean checkExistenceOfAllFolderInFolders(List<MagitSingleFolder> i_Folders, List<String> i_Errors) {
        Set<String> setOfFolderIds = new HashSet<>();
        Set<String> setOfFolderIdsInSingleFolders = new HashSet<>();
        boolean checkExistenceOfAllFoldersInFolders = false;

        for (MagitSingleFolder folder : i_Folders) {
            setOfFolderIds.add(folder.getId());
        }

        for(MagitSingleFolder folder: i_Folders) {
            setOfFolderIdsInSingleFolders.add(folder.getId());
            getAllFoldersIdsInSingleFoldersRec(i_Folders, folder, setOfFolderIdsInSingleFolders);
        }

        if(setOfFolderIds.size() == setOfFolderIdsInSingleFolders.size()) {
            setOfFolderIds.removeAll(setOfFolderIdsInSingleFolders);
            if(setOfFolderIds.size() == 0) {
                checkExistenceOfAllFoldersInFolders = true;
            }
        }

        if(!checkExistenceOfAllFoldersInFolders){
            i_Errors.add("There is a reference from a folder to another folder that does not exist");
        }

        return checkExistenceOfAllFoldersInFolders;
    }

    private void getAllFoldersIdsInSingleFoldersRec(List<MagitSingleFolder> i_Folders, MagitSingleFolder i_Folder, Set<String> i_SetOfFolderIdsInSingleFolders){
        List<Item> items = i_Folder.getItems().getItem();
        MagitSingleFolder nextFolder = null;

        if(!checkIfAllTypesAreBlobs(items)) {
            for (Item item : items) {
                if (item.getType().equals("folder")) {
                    i_SetOfFolderIdsInSingleFolders.add(item.getId());
                    for (MagitSingleFolder folder : i_Folders) {
                        if (folder.getId().equals(item.getId())) {
                            nextFolder = folder;
                            break;
                        }
                    }
                    getAllFoldersIdsInSingleFoldersRec(i_Folders, nextFolder, i_SetOfFolderIdsInSingleFolders);
                }
            }
        }
    }

    private boolean checkIfAllTypesAreBlobs(List<Item> i_Items) {
        boolean checkIfAllTypesAreBlobs = true;

        for(Item item : i_Items)
        {
            if(item.getType().equals("folder"))
            {
                checkIfAllTypesAreBlobs = false;
            }
        }

        return  checkIfAllTypesAreBlobs;
    }

    //check 3.5
    private boolean checkIfFolderIdIsInInnerFolders(List<MagitSingleFolder> i_Folders, List<String> i_Errors) {
        List<Item> items = null;
        boolean checkIfFolderIdIsInInnerFolders = true;

        for(MagitSingleFolder folder : i_Folders){
            items = folder.getItems().getItem();
            for(Item item : items){
                if(item.getType().equals("folder")) {
                    if (folder.getId().equals(item.getId())) {
                        checkIfFolderIdIsInInnerFolders = false;
                        break;
                    }
                }
            }
        }

        if(!checkIfFolderIdIsInInnerFolders){
            i_Errors.add("There is a reference from a folder to itself");
        }

        return checkIfFolderIdIsInInnerFolders;
    }

    // check 3.6
    private boolean checkExistenceOfReferencesFromCommitsToFolders(List<MagitSingleCommit> i_Commits, List<MagitSingleFolder> i_Folders, List<String> i_Errors) {
        boolean checkExistenceOfReferencesFromCommitsToFolders = true;
        Set<String> setOfFoldersIds = new HashSet<>();

        for (MagitSingleFolder folder : i_Folders) {
            setOfFoldersIds.add(folder.getId());
        }

        for (MagitSingleCommit commit : i_Commits) {
            // true if this set did not already contain the specified id
            if (setOfFoldersIds.add(commit.getRootFolder().getId())) {
                checkExistenceOfReferencesFromCommitsToFolders = false;
                break;
            }
        }

        if(!checkExistenceOfReferencesFromCommitsToFolders){
            i_Errors.add("There is a reference from a commit to folder that does not exist");
        }

        return checkExistenceOfReferencesFromCommitsToFolders;
    }

    // check 3.7
    private boolean checkReferencesFromCommitsToFoldersValidity(List<MagitSingleCommit> i_Commits, List<MagitSingleFolder> i_Folders, List<String> i_Errors) {
        boolean checkReferencesFromCommitsToFoldersValidity = true;
        Set<String> setOfRootFoldersIds = new HashSet<>();

        for (MagitSingleFolder folder : i_Folders) {
            if (folder.isIsRoot()) {
                setOfRootFoldersIds.add(folder.getId());
            }
        }

        for (MagitSingleCommit commit : i_Commits) {
            // true if this set did not already contain the specified id
            if (setOfRootFoldersIds.add(commit.getRootFolder().getId())) {
                checkReferencesFromCommitsToFoldersValidity = false;
                break;
            }
        }

        if(!checkReferencesFromCommitsToFoldersValidity){
            i_Errors.add("There is a reference from a commit to folder that does not a root folder");
        }

        return checkReferencesFromCommitsToFoldersValidity;
    }

    // check 3.8
    private boolean checkBranchesValidity(List<MagitSingleBranch> i_Branches, List<MagitSingleCommit> i_Commits, List<String> i_Errors) {
        boolean checkBranchesValidity = true;

        if(i_Commits.size() != 0) {
            Set<String> setOfCommitsIds = new HashSet<>();

            for (MagitSingleCommit commit : i_Commits) {
                setOfCommitsIds.add(commit.getId());
            }

            for (MagitSingleBranch branch : i_Branches) {
                // true if this set did not already contain the specified id
                if (setOfCommitsIds.add(branch.getPointedCommit().getId())) {
                    checkBranchesValidity = false;
                    break;
                }
            }

            if (!checkBranchesValidity) {
                i_Errors.add("There is a branch that points to an undefined commit");
            }
        }

        return checkBranchesValidity;
    }

    // check 3.9
    private boolean isHeadValid(MagitBranches i_MagitBranches, List<String> i_Errors) {
        boolean isHeadValid = false;
        String head = i_MagitBranches.getHead();
        List<MagitSingleBranch> branches = i_MagitBranches.getMagitSingleBranch();

        for(MagitSingleBranch branch : branches){
            if(branch.getName().equals(head)){
                isHeadValid = true;
                break;
            }
        }

        if(!isHeadValid){
            i_Errors.add("The Head points to a name of undefined branch");
        }

        return isHeadValid;
    }

    // check 3.10
    private boolean isRemoteValid(List<String> i_Errors) {
        boolean isRemoteValid = true;

        if(m_MagitRepository.getMagitRemoteReference() != null && m_MagitRepository.getMagitRemoteReference().getLocation() != null) {
            isRemoteValid = FileUtilities.isRepositoryFileAlreadyExists(m_MagitRepository.getMagitRemoteReference().getLocation());
        }

        if(!isRemoteValid){
            i_Errors.add("There is no repository in the remote location");
        }

        return isRemoteValid;
    }

    // check 3.11
    private boolean isTrackingAfterValid(List<MagitSingleBranch> i_Branches, List<String> i_Errors) {
        boolean isTrackingAfterValid = true;
        String trackingAfterBranchName;

        for(MagitSingleBranch magitBranch1: i_Branches){
            if(magitBranch1.isTracking()){
                trackingAfterBranchName = magitBranch1.getTrackingAfter();
                for(MagitSingleBranch magitBranch2: i_Branches){
                    if(magitBranch2.getName().equals(trackingAfterBranchName)){
                        if(!magitBranch2.isIsRemote()){
                            isTrackingAfterValid = false;
                            break;
                        }
                    }
                }
                if(!isTrackingAfterValid)
                {
                    break;
                }
            }
        }

        if(!isTrackingAfterValid){
            i_Errors.add("There is a branch that tracking after a branch that isn't remote");
        }

        return isTrackingAfterValid;
    }
}
