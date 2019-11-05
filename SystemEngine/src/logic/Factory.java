package logic;

import jaxb.schema.generated.*;
import org.apache.commons.codec.digest.DigestUtils;
import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Factory {
    public static Repository createRepositoryFromXML(MagitRepository i_MagitRepository) throws IOException {
        Repository repository = new Repository(i_MagitRepository.getName(), i_MagitRepository.getLocation());

        if(i_MagitRepository.getMagitRemoteReference() != null && i_MagitRepository.getMagitRemoteReference().getLocation() != null){
            repository.setRemoteRepositoryLocation(i_MagitRepository.getMagitRemoteReference().getLocation());
            repository.setRemoteRepositoryName(i_MagitRepository.getMagitRemoteReference().getName());
        }

        createBranchesFromXML(repository, repository.getBranches(), i_MagitRepository.getMagitBranches());
        createCommitsFromXML(repository.getCommits(), i_MagitRepository.getMagitCommits().getMagitSingleCommit());
        createFoldersFromXML(repository.getFolders(), i_MagitRepository.getMagitFolders().getMagitSingleFolder(), i_MagitRepository.getMagitBlobs().getMagitBlob());
        createBlobsFromXML(repository.getBlobs(), i_MagitRepository.getMagitBlobs().getMagitBlob());
        createFileSystem(repository);
        repository.FixItemsID();

        return repository;
    }

    private static void createBranchesFromXML(Repository repository, Map<String, Branch> i_Branches, MagitBranches i_magitBranches) {
        List<MagitSingleBranch> magitBranches = i_magitBranches.getMagitSingleBranch();

        for (MagitSingleBranch magitBranch : magitBranches) {
            Branch newBranch = createBranch(magitBranch.getName(), magitBranch.getPointedCommit().getId(), magitBranch.getTrackingAfter(), magitBranch.isIsRemote(), magitBranch.isTracking());

            i_Branches.put(magitBranch.getName(), newBranch);
            if (i_magitBranches.getHead().equals(magitBranch.getName())) {
                newBranch.setIsHead(true);
                repository.setHeadBranch(newBranch);
            }
        }
    }

    private static void createCommitsFromXML(Map<String, Commit> i_Commits, List<MagitSingleCommit> i_MagitCommits) {
        String firstPrecedingCommitId;
        String secondPrecedingCommitId = "";

        for (MagitSingleCommit magitCommit : i_MagitCommits) {
            if (magitCommit.getPrecedingCommits() == null || magitCommit.getPrecedingCommits().getPrecedingCommit().size() == 0) {
                firstPrecedingCommitId = "";
            } else {
                firstPrecedingCommitId = magitCommit.getPrecedingCommits().getPrecedingCommit().get(0).getId();

                if(magitCommit.getPrecedingCommits().getPrecedingCommit().size() > 1){
                    secondPrecedingCommitId = magitCommit.getPrecedingCommits().getPrecedingCommit().get(1).getId();
                }
            }

            Commit newCommit = createCommit(magitCommit.getRootFolder().getId(), firstPrecedingCommitId, magitCommit.getMessage(), magitCommit.getAuthor(), magitCommit.getDateOfCreation());
            newCommit.setSecondPrecedingCommitId(secondPrecedingCommitId);
            i_Commits.put(magitCommit.getId(), newCommit);

            if (magitCommit.getPrecedingCommits() != null && magitCommit.getPrecedingCommits().getPrecedingCommit().size() > 1 && magitCommit.getPrecedingCommits().getPrecedingCommit().get(1) != null
                    && !magitCommit.getPrecedingCommits().getPrecedingCommit().get(1).equals("") && !magitCommit.getPrecedingCommits().getPrecedingCommit().get(1).equals("null")) {
                newCommit.setSecondPrecedingCommitId(magitCommit.getPrecedingCommits().getPrecedingCommit().get(1).getId());
            }
        }
    }

    private static void createFoldersFromXML(Map<String, Folder> i_Folders ,List<MagitSingleFolder> i_MagitFolders, List<MagitBlob> i_MagitBlobs) {
        HashMap<String, MagitBlob> magitBlobsHashMap = new HashMap<>();
        HashMap<String, MagitSingleFolder> magitFoldersHashMap = new HashMap<>();

        //get magitHashMaps
        for(MagitBlob blob : i_MagitBlobs) {
            magitBlobsHashMap.put(blob.getId(), blob);
        }

        for(MagitSingleFolder folder : i_MagitFolders) {
            magitFoldersHashMap.put(folder.getId(), folder);
        }

        transformMagitFolderHashMapToFolderHashMap(i_Folders, magitFoldersHashMap, magitBlobsHashMap);
    }

    private static void transformMagitFolderHashMapToFolderHashMap(Map<String, Folder> i_Folders, HashMap<String, MagitSingleFolder> i_MagitFoldersHashMap, Map<String, MagitBlob> i_MagitBlobsHashMap) {
        for(Map.Entry<String, MagitSingleFolder> folder : i_MagitFoldersHashMap.entrySet()) {
            if(!i_Folders.containsKey(folder.getValue().getId())) {
                Folder folderToPut = new Folder(folder.getValue().isIsRoot());
                i_Folders.put(folder.getValue().getId(), folderToPut);
            }

            for (Item item : folder.getValue().getItems().getItem()) {
                boolean isFolder = item.getType().equals("folder");
                Folder.ItemData folderItem = new Folder.ItemData();

                folderItem.setId(item.getId());
                folderItem.setType(isFolder ?
                        Folder.ItemData.eItemType.FOLDER :
                        Folder.ItemData.eItemType.BLOB);
                folderItem.setLastUpdater(isFolder ?
                        i_MagitFoldersHashMap.get(item.getId()).getLastUpdater() :
                        i_MagitBlobsHashMap.get(item.getId()).getLastUpdater());
                folderItem.setLastUpdateDate(isFolder ?
                        i_MagitFoldersHashMap.get(item.getId()).getLastUpdateDate() :
                        i_MagitBlobsHashMap.get(item.getId()).getLastUpdateDate());
                folderItem.setName(isFolder ?
                        i_MagitFoldersHashMap.get(item.getId()).getName() :
                        i_MagitBlobsHashMap.get(item.getId()).getName());

                i_Folders.get(folder.getValue().getId()).getItems().add(folderItem);
            }
        }
    }

    private static void createBlobsFromXML(Map<String, Blob> i_Blobs, List<MagitBlob> i_MagitBlobs) {
        for (MagitBlob magitBlob : i_MagitBlobs) {
            Blob newBlob = createBlob(magitBlob.getContent());
            i_Blobs.put(magitBlob.getId(), newBlob);
        }
    }

    public static Branch createBranch(String i_Name, String i_PointedCommit, boolean i_IsHead) {
        return new Branch(i_Name, i_PointedCommit, i_IsHead);
    }

    public static Branch createBranch(String i_Name, String i_PointedCommit, String i_TrackingAfter, boolean i_IsRemote, boolean i_IsTracking) {
        return new Branch(i_Name, i_PointedCommit, i_TrackingAfter, i_IsRemote, i_IsTracking);
    }

    public static Commit createCommit(String i_RootFolderId, String i_PrecedingCommitId, String i_Message, String i_Author){
        return new Commit(i_RootFolderId, i_PrecedingCommitId, i_Message, i_Author);
    }

    public static Commit createCommit(String i_RootFolderId, String i_PrecedingCommitId, String i_Message, String i_Author, String i_DateOfCreation) {
        return new Commit(i_RootFolderId, i_PrecedingCommitId, i_Message, i_Author, i_DateOfCreation);
    }

    public static Commit createCommit(String i_RootFolderId, String i_PrecedingCommitId1, String i_PrecedingCommitId2, String i_Message, String i_Author, String i_DateOfCreation){
        return new Commit(i_RootFolderId, i_PrecedingCommitId1, i_PrecedingCommitId2, i_Message, i_Author, i_DateOfCreation);
    }

    public static Folder createFolder(boolean i_IsRoot){
        return new Folder(i_IsRoot);
    }

    public static Blob createBlob(String i_Content){
        return new Blob(i_Content);
    }

    public static Folder.ItemData createItemData(String i_Name, String i_Id, Folder.ItemData.eItemType i_Type, String i_LastUpdater, String i_LastUpdateDate){
        return new Folder.ItemData(i_Name, i_Id, i_Type, i_LastUpdater, i_LastUpdateDate);
    }

    public static Folder.ItemData createItemData(String i_Name, String i_Id, Folder.ItemData.eItemType i_Type, String i_LastUpdater){
        return new Folder.ItemData(i_Name, i_Id, i_Type, i_LastUpdater);
    }

    public static void initializeRepositoryInFileSystem(String i_Location) {
        File repository = new File(i_Location);
        File magit = new File(repository, ".magit");
        File objects = new File(magit, "objects");
        File Branches = new File(magit, "branches");

        repository.mkdir();
        magit.mkdir();
        objects.mkdir();
        Branches.mkdir();
    }

    public static void createFileSystem(Repository i_ActiveRepository) throws IOException {
        createDotMagitInFileSystem(i_ActiveRepository);
        createWorkingCopyInFileSystem(i_ActiveRepository);
    }

    private static void createDotMagitInFileSystem(Repository i_ActiveRepository) throws IOException {
        Map<String, Branch> branches = i_ActiveRepository.getBranches();
        Map<String, Commit> commits = i_ActiveRepository.getCommits();

        initializeRepositoryInFileSystem(i_ActiveRepository.getLocation());
        FileUtilities.writeToFile(Paths.get(i_ActiveRepository.getLocation(),
                ".magit", "repositoryName.txt").toString(),
                i_ActiveRepository.getName());
        if (i_ActiveRepository.getRemoteRepositoryLocation() != null) {
            new File(i_ActiveRepository.getLocation() + "//.magit//branches//" + i_ActiveRepository.getRemoteRepositoryName()).mkdir();
        }

        for (Map.Entry<String, Branch> branch : branches.entrySet()) {
            String pointedCommitSha1 = branch.getValue().getPointedCommitId();

            if (!branch.getValue().getIsRemote()) {
                if (pointedCommitSha1 != null && !pointedCommitSha1.equals("")) {
                    Commit commit = commits.get(pointedCommitSha1);
                    pointedCommitSha1 = spreadCommitToFileSystem(i_ActiveRepository, commit, false);
                    branch.getValue().setPointedCommitId(pointedCommitSha1);
                    FileUtilities.writeToFile(Paths.get(i_ActiveRepository.getLocation(),
                            ".magit", "branches", branch.getValue().getName().concat(".txt")).toString(),
                            pointedCommitSha1);
                }
            }
        }
    }

    public static void createWorkingCopyInFileSystem(Repository i_ActiveRepository) throws IOException {
        Branch headBranch = i_ActiveRepository.getHeadBranch();
        FileUtilities.writeToFile(Paths.get(i_ActiveRepository.getLocation(),
                ".magit", "branches", "Head.txt").toString(), headBranch.getName());
        String pointedCommitSha1 = headBranch.getPointedCommitId();

        if (pointedCommitSha1 != null && !pointedCommitSha1.equals("")) {
            Commit currentCommit = i_ActiveRepository.getCommits().get(pointedCommitSha1);
            String rootFolderSha1 = currentCommit.getRootFolderId();
            Folder rootFolder = i_ActiveRepository.getFolders().get(rootFolderSha1);
            spreadCommitToFileSystemRec(i_ActiveRepository, rootFolder, "", i_ActiveRepository.getLocation(), true);
        } else {
            File file = new File(Paths.get(i_ActiveRepository.getLocation(),
                    ".magit", "branches", headBranch.getName() + ".txt").toString());
            file.createNewFile();
        }
    }

    private static String spreadCommitToFileSystem(Repository i_ActiveRepository, Commit i_Commit, boolean i_IsCreateWC) throws IOException {
        String precedingCommitSha1 = i_Commit.getFirstPrecedingCommitId();
        String precedingCommitSha2 = i_Commit.getSecondPrecedingCommitId();
        String rootFolderSha1 = i_Commit.getRootFolderId();
        Folder rootFolder = i_ActiveRepository.getFolders().get(rootFolderSha1);
        String objectsPath = Paths.get(i_ActiveRepository.getLocation(),
                ".magit", "objects").toString();

        if(i_Commit.getFirstPrecedingCommitId() != null && !i_Commit.getFirstPrecedingCommitId().equals("null") && !i_Commit.getFirstPrecedingCommitId().equals("")) {
            i_Commit.setFirstPrecedingCommitId(spreadCommitToFileSystem(i_ActiveRepository, i_ActiveRepository.getCommits().get(precedingCommitSha1), i_IsCreateWC));
        }

        if(i_Commit.getSecondPrecedingCommitId() != null && !i_Commit.getSecondPrecedingCommitId().equals("null") && !i_Commit.getSecondPrecedingCommitId().equals("")) {
            i_Commit.setSecondPrecedingCommitId(spreadCommitToFileSystem(i_ActiveRepository, i_ActiveRepository.getCommits().get(precedingCommitSha2), i_IsCreateWC));
        }

        rootFolderSha1 = spreadCommitToFileSystemRec(i_ActiveRepository, rootFolder, "", "", i_IsCreateWC);
        i_ActiveRepository.getFolders().put(rootFolderSha1, rootFolder);
        i_Commit.setRootFolderId(rootFolderSha1);
        String commitSha1 = DigestUtils.sha1Hex(i_Commit.toStringForSha1());
        String zipPath = Paths.get(objectsPath, commitSha1).toString();

        FileUtilities.zip(commitSha1, i_Commit.toString(), zipPath);
        i_ActiveRepository.getCommits().put(commitSha1, i_Commit);

        return commitSha1;
    }

    private static String spreadCommitToFileSystemRec(Repository i_ActiveRepository, Object i_File, String i_FileNameInFolder, String i_CurrentPath,  boolean i_IsInWCCreation) throws IOException {
        String sha1;
        String objectsPath = Paths.get(i_ActiveRepository.getLocation(),
                ".magit", "objects").toString();

        if(Blob.class.isInstance(i_File)) { // is Blob
            String blobContent = ((Blob)i_File).getContent();
            sha1 = DigestUtils.sha1Hex(blobContent);
            String zipPath = Paths.get(objectsPath, sha1).toString();

            if(i_IsInWCCreation) {
                FileUtilities.writeToFile(Paths.get(i_CurrentPath, i_FileNameInFolder).toString(), blobContent);
            }

            FileUtilities.zip(sha1, blobContent, zipPath); // open new zip file
        }
        else { // is folder
            Folder folder = (Folder)i_File;
            String folderPath = Paths.get(i_CurrentPath, i_FileNameInFolder).toString();

            if(!i_FileNameInFolder.equals("") && !i_CurrentPath.equals("")) { // creates the folder
                File folderToCreate = new File(folderPath);
                folderToCreate.mkdir();
            }

            for(Folder.ItemData file: folder.getItems()) {
                Object item = file.getType() == Folder.ItemData.eItemType.BLOB ?
                        i_ActiveRepository.getBlobs().get(file.getId()) :
                        i_ActiveRepository.getFolders().get(file.getId());
                String itemSha1 = spreadCommitToFileSystemRec(i_ActiveRepository, item, file.getName(), Paths.get(i_CurrentPath, i_FileNameInFolder).toString(), i_IsInWCCreation);

                if(file.getType() == Folder.ItemData.eItemType.BLOB) { // ADD AGAIN = SHA1 CHANGED
                    i_ActiveRepository.getBlobs().put(itemSha1,
                            i_ActiveRepository.getBlobs().get(file.getId()));
                }
                else {
                    i_ActiveRepository.getFolders().put(itemSha1,
                            i_ActiveRepository.getFolders().get(file.getId()));
                }

                file.setId(itemSha1); // change his id to sha1
            }

            sha1 = DigestUtils.sha1Hex(folder.toString()); // make files on "objects"
            String zipPath = Paths.get(objectsPath, sha1).toString();

                FileUtilities.zip(sha1, folder.toString(), zipPath);
        }

        return sha1;
    }

    public static Branch createNewBranchInFileSystem(Repository i_ActiveRepository, String i_BranchNameToCreate, String i_PointedCommitSha1) throws IOException {
        FileUtilities.writeToFile(Paths.get(i_ActiveRepository.getLocation(),
                ".magit", "branches", i_BranchNameToCreate.concat(".txt")).toString(),
                i_PointedCommitSha1);

        return createBranch(i_BranchNameToCreate, i_PointedCommitSha1, false);
    }

    public static Branch createRemoteTrackingBranchInFileSystem(Repository i_ActiveRepository, String i_BranchNameToCreate, String i_PointedCommitSha1, String i_RemoteBranchName) throws IOException {
        FileUtilities.writeToFile(Paths.get(i_ActiveRepository.getLocation(),
                ".magit", "branches", i_BranchNameToCreate.concat(".txt")).toString(),
                i_PointedCommitSha1);

        return createBranch(i_BranchNameToCreate, i_PointedCommitSha1, i_RemoteBranchName, false, true);
    }
}
