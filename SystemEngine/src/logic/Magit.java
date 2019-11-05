package logic;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Magit {
    private String m_ActiveUserName = "Administrator";
    private Repository m_ActiveRepository = null;
    private ReadFromXML m_DataFromXML;
    private WriteToXML m_ExportRepositoryToXML = null;
    private volatile boolean m_TaskFlag = true;

    public enum FileStatusCompareAncestor {
        SAME, DELETED, ADDED, CHANGED
    }

    public boolean getTaskFlag() {
        return m_TaskFlag;
    }

    public String getActiveUserName() {
        return m_ActiveUserName;
    }

    public Repository getActiveRepository() {
        return m_ActiveRepository;
    }

    public ReadFromXML getDataFromXML() {
        return m_DataFromXML;
    }

    public void setActiveUserName(String i_ActiveUserName) {
        this.m_ActiveUserName = i_ActiveUserName;
    }

    public void setTaskFlag(boolean m_TaskFlag) {
        this.m_TaskFlag = m_TaskFlag;
    }

    public boolean isXMLValid(String i_XMLFullPath, List<String> i_Errors) throws JAXBException, FileNotFoundException {
        m_DataFromXML = new ReadFromXML(i_XMLFullPath);

        return m_DataFromXML.isXMLValid(i_Errors);
    }

    public boolean isRepositoryFileAlreadyExists() {
        return (m_ActiveRepository != null &&
                m_ActiveRepository.getLocation() == m_DataFromXML.getMagitRepository().getLocation()) ||
                FileUtilities.isRepositoryFileAlreadyExists(m_DataFromXML.getMagitRepository().getLocation()) == true;
    }

    public void spreadHeadBranchInFileSystemToOurObjects(String i_RepositoryFullPath) throws IOException {
        m_ActiveRepository = createRepositoryFromFileSystemToObject(i_RepositoryFullPath);
    }

    public Repository createRepositoryFromFileSystemToObject(String i_RepositoryFullPath) throws IOException {
        Repository resultRepository;

        File file = new File(i_RepositoryFullPath + "//.magit//branches//Head.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String activeBranchName = br.readLine();
        br.close();
        file = new File(i_RepositoryFullPath + "//.magit//repositoryName.txt");
        br = new BufferedReader(new FileReader(file));
        String activeRepositoryName = br.readLine();
        br.close();
        String remoteRepoLocation = null;
        file = new File(i_RepositoryFullPath + "//.magit//RRLocation.txt");

        if (file.exists()) {
            br = new BufferedReader(new FileReader(file));
            remoteRepoLocation = br.readLine();
            br.close();
        }

        resultRepository = new Repository(activeRepositoryName, i_RepositoryFullPath);
        resultRepository.setName(activeRepositoryName);
        resultRepository.setLocation(i_RepositoryFullPath);

        if (remoteRepoLocation != null && !remoteRepoLocation.isEmpty() && remoteRepoLocation != "null") {
            resultRepository.setRemoteRepositoryLocation(remoteRepoLocation);
        }

        spreadBranchesInFileSystemToOurObjects(i_RepositoryFullPath, resultRepository);

        Branch headBranch = resultRepository.getBranches().get(activeBranchName);
        headBranch.setIsHead(true);
        resultRepository.setHeadBranch(headBranch);

        return resultRepository;
    }

    private void spreadBranchesInFileSystemToOurObjects(String i_RepositoryFullPath, Repository i_RepositoryToMake) throws IOException {
        String branchesPath = "//.magit//branches//";

        File file = new File(i_RepositoryFullPath + branchesPath);
        File[] files = file.listFiles();
        BufferedReader br;
        String pointedCommitId;
        assert files != null;
        List<File> RRFolder = Arrays.stream(files).filter(File::isDirectory).collect(Collectors.toList());

        if (!RRFolder.isEmpty()) {
            File remoteFolder = RRFolder.get(0);
            File[] filesInRemoteFolder = remoteFolder.listFiles();
            i_RepositoryToMake.setRemoteRepositoryName(remoteFolder.getName());
            assert filesInRemoteFolder != null;
            for (File f : filesInRemoteFolder) {
                br = new BufferedReader(new FileReader(f));
                pointedCommitId = br.readLine();
                br.close();
                Branch remoteBranch = Factory.createBranch(remoteFolder.getName() + "\\" + f.getName().substring(0, f.getName().length() - 4), pointedCommitId, null, true, false);
                i_RepositoryToMake.getBranches().put(remoteBranch.getName(), remoteBranch);
                if (pointedCommitId != null && !pointedCommitId.equals("null") && !pointedCommitId.equals("")) {
                    spreadCommitsInFileSystemToOurObjectsRec(i_RepositoryToMake.getLocation(), pointedCommitId, i_RepositoryToMake);
                }
            }
        }

        List<String> branchesNames = FileUtilities.getFilenamesOfAllFilesInAFolder(i_RepositoryToMake.getLocation() + "//.magit//branches");
        branchesNames.remove(branchesNames.indexOf("Head.txt"));
        List<String> branchesNamesWithoutTxtSuffix = getBranchesNamesWithoutTxtSuffix(branchesNames);

        for (String branchNameWithoutTxtSuffix : branchesNamesWithoutTxtSuffix) {
            if (!i_RepositoryToMake.getBranches().containsKey(branchNameWithoutTxtSuffix)) {
                file = new File(i_RepositoryToMake.getLocation() + branchesPath + branchNameWithoutTxtSuffix + ".txt");
                br = new BufferedReader(new FileReader(file));
                pointedCommitId = br.readLine();
                br.close();

                if (!RRFolder.isEmpty()) {
                    File remoteFolder = RRFolder.get(0);
                    if (i_RepositoryToMake.getBranches().containsKey(remoteFolder.getName() + "\\" + branchNameWithoutTxtSuffix)) {
                        Branch branch = Factory.createBranch(branchNameWithoutTxtSuffix, pointedCommitId, remoteFolder.getName() + "\\" + branchNameWithoutTxtSuffix, false, true);
                        i_RepositoryToMake.getBranches().put(branchNameWithoutTxtSuffix, branch);
                    } else {
                        Branch branch = Factory.createBranch(branchNameWithoutTxtSuffix, pointedCommitId, null, false, false);
                        i_RepositoryToMake.getBranches().put(branchNameWithoutTxtSuffix, branch);
                    }
                } else {
                    Branch branch = Factory.createBranch(branchNameWithoutTxtSuffix, pointedCommitId, null, false, false);
                    i_RepositoryToMake.getBranches().put(branchNameWithoutTxtSuffix, branch);
                }

                if (pointedCommitId != null && !pointedCommitId.equals("null") && !pointedCommitId.equals("")) {
                    spreadCommitsInFileSystemToOurObjectsRec(i_RepositoryToMake.getLocation(), pointedCommitId, i_RepositoryToMake);
                }
            }
        }
    }

    private List<String> getBranchesNamesWithoutTxtSuffix(List<String> i_BranchesNames) {
        List<String> branchesNamesWithoutTxtSuffix = new ArrayList<>();
        String txtSuffix = ".txt";

        for (String branchesName : i_BranchesNames) {
            int index = branchesName.lastIndexOf(txtSuffix);
            if (index > 0) {
                branchesNamesWithoutTxtSuffix.add(branchesName.substring(0, index));
            }
        }

        return branchesNamesWithoutTxtSuffix;
    }

    private void spreadCommitsInFileSystemToOurObjectsRec(String i_RepositoryFullPath, String i_CommitId, Repository i_RepositoryToMake) throws IOException {
        if (i_CommitId != null && !i_CommitId.equals("null") && !i_CommitId.equals("")) {
            String commitContent = FileUtilities.unZip(i_RepositoryFullPath + "//.magit//objects//" + i_CommitId);
            String[] splittedCommitContent = commitContent.split(";");
            String rootFolderId = splittedCommitContent[0];
            String precedingCommitId1 = splittedCommitContent[1];
            String precedingCommitId2 = splittedCommitContent[2];
            String message = splittedCommitContent[3];
            String author = splittedCommitContent[4];
            String dateOfCreation = splittedCommitContent[5].trim();

            if (!i_RepositoryToMake.getCommits().containsKey(i_CommitId)) {
                i_RepositoryToMake.getCommits().put(i_CommitId, Factory.createCommit(rootFolderId, precedingCommitId1, precedingCommitId2, message, author, dateOfCreation));
                if (!i_RepositoryToMake.getFolders().containsKey(rootFolderId)) {
                    i_RepositoryToMake.getFolders().put(rootFolderId, Factory.createFolder(true));
                    spreadFolderInFileSystemToOurObjectsRec(i_RepositoryToMake.getFolders().get(rootFolderId), i_RepositoryFullPath, rootFolderId, i_RepositoryToMake);
                }
            }
            if(!precedingCommitId1.isEmpty() && !precedingCommitId1.equals("null") ) {
                spreadCommitsInFileSystemToOurObjectsRec(i_RepositoryFullPath, precedingCommitId1, i_RepositoryToMake);
            }
            if(!precedingCommitId2.isEmpty() && !precedingCommitId2.equals("null") ) {
                spreadCommitsInFileSystemToOurObjectsRec(i_RepositoryFullPath, precedingCommitId2, i_RepositoryToMake);
            }
        }
    }

    private void spreadFolderInFileSystemToOurObjectsRec(Folder i_currentFolder, String i_RepositoryFullPath, String i_FolderId, Repository i_RepositoryToMake) throws IOException {
        String folderContent = FileUtilities.unZip(i_RepositoryFullPath + "//.magit//objects//" + i_FolderId);
        String[] lines = folderContent.split(System.getProperty("line.separator"));

        for (int i = 0; i < lines.length; i++) {
            String[] splittedItemInFolderContent = lines[i].split(";");
            String name = splittedItemInFolderContent[0];
            String id = splittedItemInFolderContent[1];
            String type = splittedItemInFolderContent[2];
            String lastUpdater = splittedItemInFolderContent[3];
            String lastUpdateDate = splittedItemInFolderContent[4].trim();
            i_currentFolder.getItems().add(Factory.createItemData(name, id, Folder.ItemData.eItemType.valueOf(type.toUpperCase()), lastUpdater, lastUpdateDate));
        }

        List<Folder.ItemData> items = i_currentFolder.getItems();
        for (Folder.ItemData itemData : items) {
            if (itemData.getType() == Folder.ItemData.eItemType.FOLDER) {
                if (!i_RepositoryToMake.getFolders().containsKey(itemData.getId())) {
                    Folder nextFolder = Factory.createFolder(false);
                    i_RepositoryToMake.getFolders().put(itemData.getId(), nextFolder);
                    spreadFolderInFileSystemToOurObjectsRec(nextFolder, i_RepositoryFullPath, itemData.getId(), i_RepositoryToMake);
                }
            } else {
                if (!i_RepositoryToMake.getBlobs().containsKey(itemData.getId())) {
                    spreadBlobInFileSystemToOurObjects(i_RepositoryFullPath, itemData.getId(), i_RepositoryToMake);
                }
            }
        }
    }

    private void spreadBlobInFileSystemToOurObjects(String i_RepositoryFullPath, String i_BlobId, Repository i_RepositoryToMake) throws IOException {
        String blobContent = FileUtilities.unZip(i_RepositoryFullPath + "//.magit//objects//" + i_BlobId).trim();

        i_RepositoryToMake.getBlobs().put(i_BlobId, Factory.createBlob(blobContent));
    }

    public void createRepositoryFromXML() throws IOException {
        m_ActiveRepository = Factory.createRepositoryFromXML(m_DataFromXML.getMagitRepository());
    }

    public void deleteRepositoryWithLocationFromXML() throws IOException {
        File directory = new File(m_DataFromXML.getMagitRepository().getLocation());
        FileUtils.deleteDirectory(directory);
    }

    public String getCurrentCommitFileSystemInformation() {

        Commit currentCommit = m_ActiveRepository.getCommits().get(m_ActiveRepository.getHeadBranch().getPointedCommitId());
        Folder rootFolder = m_ActiveRepository.getFolders().get(currentCommit.getRootFolderId());

        return getCurrentFolderFileSystemInformationRec(rootFolder, m_ActiveRepository.getLocation());
    }

    private String getCurrentFolderFileSystemInformationRec(Folder i_CurrentFolder, String i_FullPath) {
        List<Folder.ItemData> items = i_CurrentFolder.getItems();
        Folder nextFolder;
        String currentFolderFileSystemInformation = "";

        if (checkIfAllTypesInAFolderAreBlobs(items)) {
            for (Folder.ItemData itemData : items) {
                currentFolderFileSystemInformation += ("Name: " + i_FullPath + "\\" + itemData.toStringForConsole());
            }
        } else {
            for (Folder.ItemData itemData : items) {
                if (itemData.getType() == Folder.ItemData.eItemType.BLOB) {
                    currentFolderFileSystemInformation += ("Name: " + i_FullPath + "\\" + itemData.toStringForConsole());
                } else {
                    nextFolder = m_ActiveRepository.getFolders().get(itemData.getId());
                    currentFolderFileSystemInformation += (("Name: " + i_FullPath + "\\" + itemData.toStringForConsole()) + getCurrentFolderFileSystemInformationRec(nextFolder, i_FullPath + "\\" + itemData.getName()));
                }
            }
        }

        return currentFolderFileSystemInformation;
    }

    private boolean checkIfAllTypesInAFolderAreBlobs(List<Folder.ItemData> i_Items) {
        boolean checkIfAllTypesAreBlobs = true;

        for (Folder.ItemData itemData : i_Items) {
            if (itemData.getType() == Folder.ItemData.eItemType.FOLDER) {
                checkIfAllTypesAreBlobs = false;
            }
        }

        return checkIfAllTypesAreBlobs;
    }

    public String getWorkingCopyStatus() throws Exception {

        String workingCopyStatus = "Repository name: " + m_ActiveRepository.getName() + "\n";
        workingCopyStatus += "Repository path: " + m_ActiveRepository.getLocation() + "\n";
        workingCopyStatus += "Active user name: " + m_ActiveUserName + "\n";

        workingCopyStatus += "Deleted files:\n" + getStringInfoFromItemDataList(getDeletedFilesInformation());
        workingCopyStatus += "Added files:\n" + getStringInfoFromItemDataList(getAddedFilesInformation());
        workingCopyStatus += "Changed files:\n" + getStringInfoFromItemDataList(getChangedFilesInformation());

        return workingCopyStatus;
    }

    private List<Folder.ItemData> getDeletedFilesInformation() {
        List<Folder.ItemData> deletedFiles = new ArrayList<>();

        if (m_ActiveRepository.getCommits().size() != 0) {
            Commit currentCommit = m_ActiveRepository.getCommits().get(m_ActiveRepository.getHeadBranch().getPointedCommitId());
            Folder rootFolder = m_ActiveRepository.getFolders().get(currentCommit.getRootFolderId());
            getDeletedFilesRec(rootFolder, m_ActiveRepository.getLocation(), deletedFiles);
        }

        return deletedFiles;
    }

    private void getDeletedFilesRec(Folder i_CurrentFolder, String i_CurrentPath, List<Folder.ItemData> i_DeletedFile) {
        for (Folder.ItemData currentItemData : i_CurrentFolder.getItems()) {
            if (currentItemData.getType() == Folder.ItemData.eItemType.FOLDER) { // is folder
                if (Files.exists(Paths.get(i_CurrentPath, currentItemData.getName()))) { // is exists

                    getDeletedFilesRec(m_ActiveRepository.getFolders().get(currentItemData.getId()),
                            Paths.get(i_CurrentPath, currentItemData.getName()).toString(), i_DeletedFile);
                } else { // is not exists = deleted
                    i_DeletedFile.add(currentItemData);
                }
            } else { // is blob
                if (!Files.exists(Paths.get(i_CurrentPath, currentItemData.getName()))) { // is not exists
                    i_DeletedFile.add(currentItemData);
                }
            }
        }
    }

    private List<Folder.ItemData> getAddedFilesInformation() throws IOException {
        Folder rootFolder = new Folder(true);
        List<Folder.ItemData> addedFiles = new ArrayList<>();
        getAddedOrChangedFilesRec(rootFolder, m_ActiveRepository.getLocation(), addedFiles, true);

        return addedFiles;
    }

    private List<Folder.ItemData> getChangedFilesInformation() throws IOException {
        List<Folder.ItemData> changedFiles = new ArrayList<>();

        if (m_ActiveRepository.getCommits().size() != 0) {
            Folder rootFolder = new Folder(true);
            getAddedOrChangedFilesRec(rootFolder, m_ActiveRepository.getLocation(), changedFiles, false);
        }

        return changedFiles;
    }

    private String getAddedOrChangedFilesRec(Folder i_CurrentFolder, String i_CurrentPath, List<Folder.ItemData> i_RequiredFiles
            , Boolean i_IsAddedFilesRequired) throws IOException {
        Folder.ItemData newItem;
        List<String> folderFileNames = FileUtilities.getFilesAndFoldersNamesOfAllFilesInAFolder(i_CurrentPath);

        for (String fileInFolderName : folderFileNames) {
            if (!fileInFolderName.toLowerCase().equals(".magit")) {
                if (!fileInFolderName.toLowerCase().contains(".")) { // folder
                    Folder folder = new Folder(false);
                    String folderSha1 = getAddedOrChangedFilesRec(folder, Paths.get(i_CurrentPath,
                            fileInFolderName).toString(), i_RequiredFiles, i_IsAddedFilesRequired);
                    if (m_ActiveRepository.getFolders().size() == 0 || !m_ActiveRepository.getFolders().containsKey(folderSha1)) { // is a new folder = added
                        newItem = Factory.createItemData(fileInFolderName, folderSha1, Folder.ItemData.eItemType.FOLDER, m_ActiveUserName);

                        if (m_ActiveRepository.getFolders().size() == 0) {
                            i_RequiredFiles.add(newItem);
                        } else if (!i_IsAddedFilesRequired && parseFromFullPathToItemData(Paths.get(i_CurrentPath, fileInFolderName).toString()) != null) { // has file with same name = changed
                            i_RequiredFiles.add(newItem);
                        } else if (parseFromFullPathToItemData(Paths.get(i_CurrentPath, fileInFolderName).toString()) == null && i_IsAddedFilesRequired) { // there is no file with same namae = added
                            i_RequiredFiles.add(newItem);
                        }
                    } else { // is already exists
                        newItem = parseFromFullPathToItemData(Paths.get(i_CurrentPath, fileInFolderName).toString());
                        if (newItem == null) { // added
                            newItem = Factory.createItemData(fileInFolderName, folderSha1, Folder.ItemData.eItemType.FOLDER, m_ActiveUserName);
                            if (i_IsAddedFilesRequired) {
                                i_RequiredFiles.add(newItem);
                            }
                        }
                    }
                } else { // blob
                    String blobContent = FileUtilities.readFileAsString(Paths.get(i_CurrentPath, fileInFolderName).toString());
                    String blobSha1 = DigestUtils.sha1Hex(blobContent);
                    if (m_ActiveRepository.getBlobs().size() == 0 || !m_ActiveRepository.getBlobs().containsKey(blobSha1)) { // is a new blob
                        newItem = Factory.createItemData(fileInFolderName, blobSha1, Folder.ItemData.eItemType.BLOB, m_ActiveUserName); // last updater changed

                        if (m_ActiveRepository.getBlobs().size() == 0) {
                            i_RequiredFiles.add(newItem);
                        } else if (!i_IsAddedFilesRequired && parseFromFullPathToItemData(Paths.get(i_CurrentPath, fileInFolderName).toString()) != null) { // has file with same name = changed
                            i_RequiredFiles.add(newItem);
                        } else if (parseFromFullPathToItemData(Paths.get(i_CurrentPath, fileInFolderName).toString()) == null && i_IsAddedFilesRequired) { // there is no file with same namae = added
                            i_RequiredFiles.add(newItem);
                        }
                    } else { // is a exists blob
                        newItem = parseFromFullPathToItemData(Paths.get(i_CurrentPath, fileInFolderName).toString());
                        if (newItem == null) { // added
                            newItem = Factory.createItemData(fileInFolderName, blobSha1, Folder.ItemData.eItemType.BLOB, m_ActiveUserName);
                            if (i_IsAddedFilesRequired) {
                                i_RequiredFiles.add(newItem);
                            }
                        }
                    }
                }

                i_CurrentFolder.getItems().add(newItem);
            }
        }

        String folderSha1 = DigestUtils.sha1Hex(i_CurrentFolder.toString());

        return folderSha1;
    }

    private String getStringInfoFromItemDataList(List<Folder.ItemData> i_ItemsData) {
        String itemsDataInfo = "";
        for (Folder.ItemData itemeData : i_ItemsData) {
            itemsDataInfo += itemeData.toStringForConsole();
        }

        return itemsDataInfo;
    }

    public boolean checkIfThereIsALoadedRepository() {
        return m_ActiveRepository != null;
    }

    public boolean isTheActiveRepository(String i_RepositoryFullPath) {
        return (checkIfThereIsALoadedRepository() && m_ActiveRepository.getLocation().equals(i_RepositoryFullPath));
    }

    public boolean checkIfThereAreCommitsInTheActiveRepository() {
        return m_ActiveRepository.getHeadBranch().getPointedCommitId() != null;
    }

    public String getCurrentBranchHistory() throws IOException {
        String currentCommitId = m_ActiveRepository.getHeadBranch().getPointedCommitId();
        Commit currentCommit = m_ActiveRepository.getCommits().get(m_ActiveRepository.getHeadBranch().getPointedCommitId());
        String precedingCommitId = currentCommit.getFirstPrecedingCommitId();

        return ("SHA-1: " + currentCommitId + "," + currentCommit.toStringForConsole()) + getCommitDataRec(precedingCommitId);
    }

    private String getCommitDataRec(String i_CurrentCommitId) throws IOException {
        String currentCommitData = "";
        String commitContent = FileUtilities.unZip(m_ActiveRepository.getLocation() + "//.magit//objects//" + i_CurrentCommitId);
        String[] splittedCommitContent = commitContent.split(";");
        String precedingCommitId1 = splittedCommitContent[1];
        String message = splittedCommitContent[3];
        String author = splittedCommitContent[4];
        String dateOfCreation = splittedCommitContent[5];

        if (precedingCommitId1 == null || precedingCommitId1.equals("null") || precedingCommitId1.equals("")) {
            currentCommitData += String.format("%s, %s, %s, %s", "SHA-1: " + i_CurrentCommitId, "Message: " + message, "Date of creation: " + dateOfCreation, "Author: " + author + "\n");
        } else {
            currentCommitData += (String.format("%s, %s, %s, %s", "SHA-1: " + i_CurrentCommitId, "Message: " + message, "Date of creation: " + dateOfCreation, "Author: " + author + "\n")
                    + getCommitDataRec(precedingCommitId1));
        }

        return currentCommitData;
    }

    public String getAvailableBranchesData() throws IOException {
        String AvailableBranchesData = "";
        Branch branch = m_ActiveRepository.getHeadBranch();
        String headBranchName = branch.getName();
        String pointedCommitId = branch.getPointedCommitId();
        String commitMessage;

        if (isEmptyActiveRepository()) {
            commitMessage = "null";
        } else {
            commitMessage = m_ActiveRepository.getCommits().get(pointedCommitId).getMessage();
        }

        AvailableBranchesData += String.format("%s, %s, %s, %s", "Head branch name: " + headBranchName, "Pointed commit SHA-1: " + pointedCommitId, "Commit message: " + commitMessage, "Is remote tracking branch: " + m_ActiveRepository.getHeadBranch().getIsTracking() + "\n");

        List<String> branchesFileNames = FileUtilities.getFilenamesOfAllFilesInAFolder(m_ActiveRepository.getLocation() + "//.magit//branches//");
        for (String branchesFileName : branchesFileNames) {
            if (!branchesFileName.toLowerCase().equals("head.txt") && !branchesFileName.equals(headBranchName + ".txt")) {
                File file = new File(m_ActiveRepository.getLocation() + "//.magit//branches//" + branchesFileName);
                BufferedReader br = new BufferedReader(new FileReader(file));
                pointedCommitId = br.readLine();
                br.close();
                String commitContent = FileUtilities.unZip(m_ActiveRepository.getLocation() + "//.magit//objects//" + pointedCommitId);
                String[] splittedCommitContent = commitContent.split(";");
                commitMessage = splittedCommitContent[3];

                AvailableBranchesData += String.format("%s, %s, %s, %s", "Branch name: " + branchesFileName.substring(0, branchesFileName.length() - 4), "Pointed commit SHA-1: " + pointedCommitId, "Commit message: " + commitMessage, "Is remote tracking branch: " + m_ActiveRepository.getBranches().get(branchesFileName.substring(0, branchesFileName.length() - 4)).getIsTracking() + "\n");
            }
        }

        return AvailableBranchesData;
    }

    public void commit(String i_CommitMessage) throws Exception {
        File file;
        BufferedReader br;
        String activeBranchName;
        String pointedCommitId;
        String branchesPath = "//.magit//branches//";

        file = new File(m_ActiveRepository.getLocation() + branchesPath);
        File[] files = file.listFiles();
        assert files != null;
        file = new File(m_ActiveRepository.getLocation() + "//.magit//branches//Head.txt");
        br = new BufferedReader(new FileReader(file));
        activeBranchName = br.readLine();
        br.close();
        file = new File(m_ActiveRepository.getLocation() + "//.magit//branches//" + activeBranchName + ".txt");
        br = new BufferedReader(new FileReader(file));
        pointedCommitId = br.readLine();
        br.close();
        Folder rootFolder = Factory.createFolder(true);
        String rootFolderID = spreadWCInFileSystemToOurObjectsRec(m_ActiveRepository.getLocation(), rootFolder);
        Commit newCommit = Factory.createCommit(rootFolderID, pointedCommitId, i_CommitMessage, m_ActiveRepository.getName());
        String newCommitSha1 = DigestUtils.sha1Hex(newCommit.toStringForSha1());
        FileUtilities.writeToFile(m_ActiveRepository.getLocation() + branchesPath +
                activeBranchName + ".txt", newCommitSha1); // write the new sha1 in head
        if (!m_ActiveRepository.getCommits().containsKey(newCommitSha1)) { // WC has changed
            m_ActiveRepository.getCommits().put(newCommitSha1, newCommit);
            String objectsPath = Paths.get(m_ActiveRepository.getLocation(),
                    ".magit", "objects").toString();
            String zipPath = Paths.get(objectsPath, newCommitSha1).toString();
            FileUtilities.zip(newCommitSha1, newCommit.toString(), zipPath); // make zip of the new commit on objects
            m_ActiveRepository.getHeadBranch().setPointedCommitId(newCommitSha1);
        }
    }

    public void commit(String i_CommitMessage, String i_PointedCommitId2) throws Exception {
        File file;
        BufferedReader br;
        String activeBranchName;
        String pointedCommitId;
        String branchesPath = "//.magit//branches//";

        file = new File(m_ActiveRepository.getLocation() + branchesPath);
        File[] files = file.listFiles();
        assert files != null;
        file = new File(m_ActiveRepository.getLocation() + "//.magit//branches//Head.txt");
        br = new BufferedReader(new FileReader(file));
        activeBranchName = br.readLine();
        br.close();
        file = new File(m_ActiveRepository.getLocation() + "//.magit//branches//" + activeBranchName + ".txt");
        br = new BufferedReader(new FileReader(file));
        pointedCommitId = br.readLine();
        br.close();
        Folder rootFolder = Factory.createFolder(true);
        String rootFolderID = spreadWCInFileSystemToOurObjectsRec(m_ActiveRepository.getLocation(), rootFolder);
        Commit newCommit = Factory.createCommit(rootFolderID, pointedCommitId, i_CommitMessage, m_ActiveUserName);
        newCommit.setSecondPrecedingCommitId(i_PointedCommitId2);
        String newCommitSha1 = DigestUtils.sha1Hex(newCommit.toStringForSha1());
        FileUtilities.writeToFile(m_ActiveRepository.getLocation() + "//.magit//branches//" +
                activeBranchName + ".txt", newCommitSha1); // write the new sha1 in head
        if (!m_ActiveRepository.getCommits().containsKey(newCommitSha1)) { // WC has changed
            m_ActiveRepository.getCommits().put(newCommitSha1, newCommit);
            String objectsPath = Paths.get(m_ActiveRepository.getLocation(),
                    ".magit", "objects").toString();
            String zipPath = Paths.get(objectsPath, newCommitSha1).toString();
            FileUtilities.zip(newCommitSha1, newCommit.toString(), zipPath); // make zip of the new commit on objects
            m_ActiveRepository.getHeadBranch().setPointedCommitId(newCommitSha1);
        }
    }

    private String spreadWCInFileSystemToOurObjectsRec(String i_Location, Folder i_CurrentFolder) throws Exception {
        Folder.ItemData newItem;
        List<String> folderFileNames = FileUtilities.getFilesAndFoldersNamesOfAllFilesInAFolder(i_Location);
        String objectsPath = Paths.get(m_ActiveRepository.getLocation(),
                ".magit", "objects").toString();

        for (String fileInFolderName : folderFileNames) {
            if (!fileInFolderName.toLowerCase().equals(".magit")) {
                if (!fileInFolderName.toLowerCase().contains(".")) { // folder
                    Folder folder = new Folder(false);
                    String folderSha1 = spreadWCInFileSystemToOurObjectsRec(Paths.get(i_Location,
                            fileInFolderName).toString(), folder);
                    if (!m_ActiveRepository.getFolders().containsKey(folderSha1)) { // is a new folder
                        m_ActiveRepository.getFolders().put(folderSha1, folder);
                        newItem = Factory.createItemData(fileInFolderName, folderSha1, Folder.ItemData.eItemType.FOLDER, m_ActiveUserName);
                        String zipPath = Paths.get(objectsPath, folderSha1).toString();
                        FileUtilities.zip(folderSha1, folder.toString(), zipPath); // make zip of the new blob in objects
                    } else { // is already exists
                        newItem = parseFromFullPathToItemData(Paths.get(i_Location, fileInFolderName).toString());
                        if (newItem == null) {
                            newItem = Factory.createItemData(fileInFolderName, folderSha1, Folder.ItemData.eItemType.FOLDER, m_ActiveUserName);
                        }
                        if(!newItem.getId().equals(folderSha1)) {
                            newItem = getItemDataWithThisSha1(folderSha1);
                        }
                    }
                } else { // blob
                    String blobContent = FileUtilities.readFileAsString(Paths.get(i_Location, fileInFolderName).toString());
                    String blobSha1 = DigestUtils.sha1Hex(blobContent);
                    Blob newBolb = Factory.createBlob(blobContent);
                    if (!m_ActiveRepository.getBlobs().containsKey(blobSha1)) { // is a new blob
                        m_ActiveRepository.getBlobs().put(blobSha1, newBolb);
                        String zipPath = Paths.get(objectsPath, blobSha1).toString();
                        FileUtilities.zip(blobSha1, blobContent, zipPath); // make zip of the new blob in objects
                        newItem = Factory.createItemData(fileInFolderName, blobSha1, Folder.ItemData.eItemType.BLOB, m_ActiveUserName); // last updater changed
                    } else { // is a exists blob
                        newItem = parseFromFullPathToItemData(Paths.get(i_Location, fileInFolderName).toString());
                        if (newItem == null) {
                            newItem = Factory.createItemData(fileInFolderName, blobSha1, Folder.ItemData.eItemType.BLOB, m_ActiveUserName);
                        }
                        if(!newItem.getId().equals(blobSha1)) {
                            newItem = getItemDataWithThisSha1(blobSha1);
                        }
                    }
                }
                i_CurrentFolder.getItems().add(newItem);
            }
        }

        String folderSha1 = DigestUtils.sha1Hex(i_CurrentFolder.toString());
        if (i_CurrentFolder.getIsRoot() && !m_ActiveRepository.getFolders().containsKey(folderSha1)) {
            String zipPath = Paths.get(objectsPath, folderSha1).toString();
            FileUtilities.zip(folderSha1, i_CurrentFolder.toString(), zipPath); // make zip of the new blob in objects
            m_ActiveRepository.getFolders().put(folderSha1, i_CurrentFolder);
        }

        return folderSha1;
    }

    public void createNewBranchInFileSystemAndInOurObjects(String i_BranchNameToCreate, String i_PointedCommitSha1) throws IOException {
        m_ActiveRepository.getBranches().put(i_BranchNameToCreate, Factory.createNewBranchInFileSystem(m_ActiveRepository, i_BranchNameToCreate, i_PointedCommitSha1));
    }

    public void createRemoteTrackingBranchInFileSystemAndInOurObjects(String i_BranchNameToCreate, String i_PointedCommitSha1, String i_RemoteBranchName) throws IOException {
        m_ActiveRepository.getBranches().put(i_BranchNameToCreate, Factory.createRemoteTrackingBranchInFileSystem(m_ActiveRepository, i_BranchNameToCreate, i_PointedCommitSha1, i_RemoteBranchName));
    }

    public void deleteBranch(String i_BranchNameToDelete) throws IOException {
        File branchToDelete = new File(Paths.get(m_ActiveRepository.getLocation(),
                ".magit", "branches", i_BranchNameToDelete.concat(".txt")).toString());
        FileUtils.deleteQuietly(branchToDelete);

        spreadHeadBranchInFileSystemToOurObjects(m_ActiveRepository.getLocation());
    }

    public void addNewRepositoryInFileSystem(String i_RepositoryFullPath, String i_RepositoryName, boolean i_isLastFolderInPathNeedToBeCreated) throws IOException {
        if (i_isLastFolderInPathNeedToBeCreated) {
            new File(i_RepositoryFullPath).mkdir();
        }

        Factory.initializeRepositoryInFileSystem(i_RepositoryFullPath);
        FileUtilities.writeToFile(Paths.get(i_RepositoryFullPath,
                ".magit", "repositoryName.txt").toString(),
                i_RepositoryName);
        FileUtilities.writeToFile(Paths.get(i_RepositoryFullPath,
                ".magit", "RRLocation.txt").toString(),
                "");
        FileUtilities.writeToFile(Paths.get(i_RepositoryFullPath,
                ".magit", "branches", "Head.txt").toString(), "master");
        File masterFile = new File(Paths.get(i_RepositoryFullPath,
                ".magit", "branches", "master.txt").toString());
        masterFile.createNewFile();
    }

    public boolean checkIfThereAreCommitsInActiveRepository() {
        return (m_ActiveRepository.getCommits().size() > 0);
    }

    public boolean isBranchExistsInFileSystem(String i_BranchName) {
        boolean isBranchExists;
        if (i_BranchName.equals("Head"))
            isBranchExists = false;
        else {
            File branchToCheck = new File(Paths.get(m_ActiveRepository.getLocation(),
                    ".magit", "branches", i_BranchName.concat(".txt")).toString());
            isBranchExists = branchToCheck.exists();
        }
        return isBranchExists;
    }

    public boolean isHeadBranch(String i_BranchName) {
        return m_ActiveRepository.getHeadBranch().getName().equals(i_BranchName);
    }

    private Folder.ItemData parseFromFullPathToItemData(String i_FullPath) {
        Folder.ItemData result = null;

        String fullPathWithoutRootFolder = i_FullPath.substring(m_ActiveRepository.getLocation().length() + 1);
        String[] paths = fullPathWithoutRootFolder.split(Pattern.quote(File.separator));
        String pointedCommitId = m_ActiveRepository.getHeadBranch().getPointedCommitId();
        if (pointedCommitId != null) {
            String rootFolderId = m_ActiveRepository.getCommits().get(pointedCommitId).getRootFolderId();
            Folder currentFolder = m_ActiveRepository.getFolders().get(rootFolderId);
            List<Folder.ItemData> itemsInCurrentFolder = currentFolder.getItems();

            for (int i = 0; i < paths.length - 1; i++) {
                String nextFolderName = paths[i];
                for (Folder.ItemData itemData : itemsInCurrentFolder) {
                    if (itemData.getName().equals(nextFolderName)) {
                        currentFolder = m_ActiveRepository.getFolders().get(itemData.getId());
                        break;
                    }
                }
                itemsInCurrentFolder = currentFolder.getItems();
            }

            for (Folder.ItemData itemData : itemsInCurrentFolder) {
                if (itemData.getName().equals(paths[paths.length - 1])) {
                    result = itemData;
                    break;
                }
            }
        }

        return result;
    }

    public void exportRepositoryToXML(String i_XmlFullPath){
        m_ExportRepositoryToXML = new WriteToXML(m_ActiveRepository);
        m_ExportRepositoryToXML.exportRepositoryToXml(i_XmlFullPath);
    }

    public void checkoutBranch(String i_NewHeadBranchName) throws IOException {
        FileUtilities.writeToFile(m_ActiveRepository.getLocation() + "//.magit//branches//Head.txt", i_NewHeadBranchName);
        deleteWCFromFileSystem();
        spreadHeadBranchInFileSystemToOurObjects(m_ActiveRepository.getLocation());
        Factory.createWorkingCopyInFileSystem(m_ActiveRepository);
    }

    public boolean isWCClean() throws IOException {
        return (getAddedFilesInformation().size() == 0 && getDeletedFilesInformation().size() == 0
                && getChangedFilesInformation().size() == 0);
    }

    public boolean isSha1ExistsInFileSystem(String i_Sha1) {

        File sha1ToCheck = new File(Paths.get(m_ActiveRepository.getLocation(),
                ".magit", "objects", i_Sha1).toString());

        return (sha1ToCheck.exists());
    }

    public void resetHeadBranch(String i_NewPointedCommitSha1) throws IOException {
        String headBranchName = m_ActiveRepository.getHeadBranch().getName();
        FileUtilities.writeToFile(m_ActiveRepository.getLocation() + "//.magit//branches//" + headBranchName + ".txt", i_NewPointedCommitSha1);
        deleteWCFromFileSystem();
        m_ActiveRepository.getBranches().get(headBranchName).setPointedCommitId(i_NewPointedCommitSha1);
        Factory.createWorkingCopyInFileSystem(m_ActiveRepository);
    }

    private void deleteWCFromFileSystem() {
        List<String> folderFileNames = FileUtilities.getFilesAndFoldersNamesOfAllFilesInAFolder(m_ActiveRepository.getLocation());

        for (String fileInFolderName : folderFileNames) {
            if (!fileInFolderName.toLowerCase().equals(".magit")) {
                File fileToDelete = new File(Paths.get(m_ActiveRepository.getLocation(), fileInFolderName).toString());
                FileUtils.deleteQuietly(fileToDelete);
            }
        }
    }

    public boolean isSha1OfPointedCommitOfHeadBranch(String i_Sha1) {
        return (i_Sha1.equals(m_ActiveRepository.getHeadBranch().getPointedCommitId()));
    }

    public CommitLogicNode buildCommitLogicTree() {
        Map<String, CommitLogicNode> commitLogicNodes = new HashMap<>();
        Map<String, Branch> branches = m_ActiveRepository.getBranches();

        for (Map.Entry<String, Branch> branch : branches.entrySet()) {
            Commit pointedCommit = m_ActiveRepository.getCommits().get(branch.getValue().getPointedCommitId());
            addOneBranchToCommitLogicTreeRec(commitLogicNodes, branch.getValue().getPointedCommitId(), pointedCommit, null);
        }

        return findRootOfCommitLogicTreeRec(commitLogicNodes.get(m_ActiveRepository.getHeadBranch().getPointedCommitId()));
    }

    private void addOneBranchToCommitLogicTreeRec(Map<String, CommitLogicNode> i_CommitLogicNodes, String i_CommitSha1, Commit i_Commit, CommitLogicNode i_Children) {
        if (i_CommitLogicNodes.containsKey(i_CommitSha1)) {
            if (i_Children != null) {
                i_CommitLogicNodes.get(i_CommitSha1).getChildrens().add(i_Children);
                i_Children.setFirstParent(i_CommitLogicNodes.get(i_CommitSha1));
            }
        } else {
            // create new CommitLogicNode
            CommitLogicNode newCommitLogicNode = new CommitLogicNode(i_Commit, i_CommitSha1, findPointingBranchNameOfCommit(i_CommitSha1));
            if (i_Children != null) {
                if (!newCommitLogicNode.getChildrens().contains(i_Children)) {
                    newCommitLogicNode.getChildrens().add(i_Children);
                }
                // update parents of my children
                if (i_Children.getFirstParent() == null) {
                    i_Children.setFirstParent(newCommitLogicNode);
                } else {
                    if (i_Children.getFirstParent() != newCommitLogicNode) {
                        i_Children.setSecondParent(newCommitLogicNode);
                    }
                }
            }

            i_CommitLogicNodes.put(i_CommitSha1, newCommitLogicNode);
            if (i_Commit.getFirstPrecedingCommitId() != null && !i_Commit.getFirstPrecedingCommitId().equals("null") && !i_Commit.getFirstPrecedingCommitId().equals("")) {
                addOneBranchToCommitLogicTreeRec(i_CommitLogicNodes, i_Commit.getFirstPrecedingCommitId(),
                        m_ActiveRepository.getCommits().get(i_Commit.getFirstPrecedingCommitId()), newCommitLogicNode);
                if (i_Commit.getSecondPrecedingCommitId() != null && !i_Commit.getSecondPrecedingCommitId().equals("null") && !i_Commit.getSecondPrecedingCommitId().equals("")) {
                    addOneBranchToCommitLogicTreeRec(i_CommitLogicNodes, i_Commit.getSecondPrecedingCommitId(),
                            m_ActiveRepository.getCommits().get(i_Commit.getSecondPrecedingCommitId()), newCommitLogicNode);
                }
            }
        }
    }

    private CommitLogicNode findRootOfCommitLogicTreeRec(CommitLogicNode i_Node) {
        if (i_Node.getFirstParent() == null) {
            return i_Node;
        } else {
            return findRootOfCommitLogicTreeRec(i_Node.getFirstParent());
        }
    }

    public String findPointingBranchNameOfCommit(String i_CommitID) {
        StringBuilder branchName = new StringBuilder();

        for (Map.Entry<String, Branch> branch : m_ActiveRepository.getBranches().entrySet()) {
            if (branch.getValue().getPointedCommitId().equals(i_CommitID)) {
                if (branchName.toString().isEmpty()) {
                    branchName = new StringBuilder(branch.getValue().getName());
                } else {
                    branchName.append(", ").append(branch.getValue().getName());
                }

            }
        }

        return branchName.toString();
    }

    public List<MergeConflict> merge(Branch i_Their) throws IOException {
        AncestorFinder ancestorFinder = new AncestorFinder((v) -> m_ActiveRepository.getCommits().get(v));
        String activeBranchCommitSha1 = m_ActiveRepository.getHeadBranch().getPointedCommitId();
        String theirBranchCommitSha1 = i_Their.getPointedCommitId();
        String ancestorSha1 = ancestorFinder.traceAncestor(activeBranchCommitSha1, theirBranchCommitSha1);

        //check if it's a fast forward merge
        if (ancestorSha1.equals(activeBranchCommitSha1) || ancestorSha1.equals(theirBranchCommitSha1)) {
            handleFastForwardMerge(ancestorSha1, m_ActiveRepository.getHeadBranch(), i_Their);
            return null;
        } else {
            List<FileFullPathAndItemData> ourFileDetails = getCommitBlobsDetails(m_ActiveRepository.getHeadBranch().getPointedCommitId());
            List<FileFullPathAndItemData> theirFileDetails = getCommitBlobsDetails(i_Their.getPointedCommitId());
            List<FileFullPathAndItemData> ancestorFileDetails = getCommitBlobsDetails(ancestorSha1);
            Map<String, FileStatusCompareAncestor> ourFilesCompareAncestor = new HashMap<>();
            Map<String, FileStatusCompareAncestor> theirFilesCompareAncestor = new HashMap<>();

            for (FileFullPathAndItemData fileDetails : ancestorFileDetails) {
                classifyFilesForSons(fileDetails, ourFileDetails, ourFilesCompareAncestor);
                classifyFilesForSons(fileDetails, theirFileDetails, theirFilesCompareAncestor);
            }

            handleWithAddedFile(ourFileDetails, ourFilesCompareAncestor);
            handleWithAddedFile(theirFileDetails, theirFilesCompareAncestor);

            return mergeTwoSons(ancestorFileDetails, ourFilesCompareAncestor, theirFilesCompareAncestor, ourFileDetails, theirFileDetails);
        }
    }

    private List<FileFullPathAndItemData> getCommitBlobsDetails(String i_CommitSha1) {
        List<FileFullPathAndItemData> commitFileDetails = new ArrayList<>();
        Commit currentCommit = m_ActiveRepository.getCommits().get(i_CommitSha1);
        Folder rootFolder = m_ActiveRepository.getFolders().get(currentCommit.getRootFolderId());

        getCurrentFolderBlobsDetailsRec(commitFileDetails, rootFolder, m_ActiveRepository.getLocation());

        return commitFileDetails;
    }

    private List<FileFullPathAndItemData> getCommitBlobsAndFoldersDetails(String i_CommitSha1) {
        List<FileFullPathAndItemData> commitFileDetails = new ArrayList<>();
        Commit currentCommit = m_ActiveRepository.getCommits().get(i_CommitSha1);
        Folder rootFolder = m_ActiveRepository.getFolders().get(currentCommit.getRootFolderId());

        getCurrentFolderBlobsAndFoldersDetailsRec(commitFileDetails, rootFolder, m_ActiveRepository.getLocation());

        return commitFileDetails;
    }

    private void getCurrentFolderBlobsDetailsRec(List<FileFullPathAndItemData> i_CommitFileDetails, Folder i_CurrentFolder, String i_FullPath) {
        List<Folder.ItemData> items = i_CurrentFolder.getItems();
        Folder nextFolder;

        if (checkIfAllTypesInAFolderAreBlobs(items)) {
            for (Folder.ItemData itemData : items) {
                FileFullPathAndItemData newFileDetails = new FileFullPathAndItemData(i_FullPath + "\\" + itemData.getName(), itemData);
                i_CommitFileDetails.add(newFileDetails);
            }
        } else {
            for (Folder.ItemData itemData : items) {
                if (itemData.getType() == Folder.ItemData.eItemType.BLOB) {
                    FileFullPathAndItemData newFileDetails = new FileFullPathAndItemData(i_FullPath + "\\" + itemData.getName(), itemData);
                    i_CommitFileDetails.add(newFileDetails);
                } else {
                    nextFolder = m_ActiveRepository.getFolders().get(itemData.getId());
                    getCurrentFolderBlobsDetailsRec(i_CommitFileDetails, nextFolder, i_FullPath + "\\" + itemData.getName());
                }
            }
        }
    }

    private void getCurrentFolderBlobsAndFoldersDetailsRec(List<FileFullPathAndItemData> i_CommitFileDetails, Folder i_CurrentFolder, String i_FullPath) {
        List<Folder.ItemData> items = i_CurrentFolder.getItems();
        Folder nextFolder;

        if (checkIfAllTypesInAFolderAreBlobs(items)) {
            for (Folder.ItemData itemData : items) {
                FileFullPathAndItemData newFileDetails = new FileFullPathAndItemData(i_FullPath + "\\" + itemData.getName(), itemData);
                i_CommitFileDetails.add(newFileDetails);
            }
        } else {
            for (Folder.ItemData itemData : items) {
                if (itemData.getType() == Folder.ItemData.eItemType.BLOB) {
                    FileFullPathAndItemData newFileDetails = new FileFullPathAndItemData(i_FullPath + "\\" + itemData.getName(), itemData);
                    i_CommitFileDetails.add(newFileDetails);
                } else {
                    FileFullPathAndItemData newFileDetails = new FileFullPathAndItemData(i_FullPath + "\\" + itemData.getName(), itemData);
                    i_CommitFileDetails.add(newFileDetails);
                    nextFolder = m_ActiveRepository.getFolders().get(itemData.getId());
                    getCurrentFolderBlobsAndFoldersDetailsRec(i_CommitFileDetails, nextFolder, i_FullPath + "\\" + itemData.getName());
                }
            }
        }
    }

    private void classifyFilesForSons(FileFullPathAndItemData i_File, List<FileFullPathAndItemData> i_SonFilesDetails, Map<String, FileStatusCompareAncestor> i_SonFilesMap) {
        List<FileFullPathAndItemData> givenFileInSon = i_SonFilesDetails.stream().filter(v -> v.getFullPath().equals(i_File.getFullPath())).collect(Collectors.toList());
        if (givenFileInSon.size() == 0) {
            i_SonFilesMap.put(i_File.getFullPath(), FileStatusCompareAncestor.DELETED);
        } else {
            if (givenFileInSon.get(0).getItemData().getId().equals(i_File.getItemData().getId())) {
                i_SonFilesMap.put(i_File.getFullPath(), FileStatusCompareAncestor.SAME);
            } else {
                i_SonFilesMap.put(i_File.getFullPath(), FileStatusCompareAncestor.CHANGED);
            }
        }
    }

    private void handleFastForwardMerge(String i_AncestorSha1, Branch i_HeadBranch, Branch i_TheirBranch) throws IOException {
        if (i_AncestorSha1.equals(i_HeadBranch.getPointedCommitId())) {
            resetHeadBranch(i_TheirBranch.getPointedCommitId());
        }
    }

    private void handleWithAddedFile(List<FileFullPathAndItemData> i_FileDetailsList, Map<String, FileStatusCompareAncestor> i_CompareToAncestor) {
        for (FileFullPathAndItemData fd : i_FileDetailsList) {
            if (!i_CompareToAncestor.containsKey(fd.getFullPath())) {
                i_CompareToAncestor.put(fd.getFullPath(), FileStatusCompareAncestor.ADDED);
            }
        }
    }

    //todo:: handle case two added files with different content but with same name
    private List<MergeConflict> mergeTwoSons(List<FileFullPathAndItemData> i_AncestorFilesList, Map<String, FileStatusCompareAncestor> i_OurClassifiedFiles, Map<String, FileStatusCompareAncestor> i_TheirsClassifiedFiles,
                                             List<FileFullPathAndItemData> i_OurFileDetails, List<FileFullPathAndItemData> i_TheirsFileDetails) throws IOException {
        List<FileFullPathAndItemData> mergeList = new ArrayList<>();
        List<MergeConflict> conflictedList = new ArrayList<>();
        MergeConflict conflict;

        for (FileFullPathAndItemData fd : i_AncestorFilesList) {
            List<FileFullPathAndItemData> currentFileInOurCommit = i_OurFileDetails.stream().filter(v -> v.getFullPath().equals(fd.getFullPath())).collect(Collectors.toList());
            List<FileFullPathAndItemData> currentFileInTheirsCommit = i_TheirsFileDetails.stream().filter(v -> v.getFullPath().equals(fd.getFullPath())).collect(Collectors.toList());
            if (i_OurClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.SAME && i_TheirsClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.SAME) {
                mergeList.add(fd);
            } else if (i_TheirsClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.SAME && i_OurClassifiedFiles.get(fd.getFullPath()) != FileStatusCompareAncestor.SAME) {
                if (i_OurClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.CHANGED) {
                    mergeList.add(currentFileInOurCommit.get(0));
                }
            } else if (i_TheirsClassifiedFiles.get(fd.getFullPath()) != FileStatusCompareAncestor.SAME && i_OurClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.SAME) {
                if (i_TheirsClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.CHANGED) {
                    mergeList.add(currentFileInTheirsCommit.get(0));
                }
            } else {
                if (!(i_TheirsClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.DELETED && i_OurClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.DELETED)) {
                    if (i_TheirsClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.CHANGED && i_OurClassifiedFiles.get(fd.getFullPath()) == FileStatusCompareAncestor.CHANGED &&
                            currentFileInOurCommit.get(0).getFullPath().equals(currentFileInTheirsCommit.get(0).getItemData().getId())) {
                        mergeList.add(currentFileInOurCommit.get(0));
                    } else {
                        String ourFDContent = "";
                        String theirFDContent = "";
                        if (!currentFileInOurCommit.isEmpty())
                            ourFDContent = m_ActiveRepository.getBlobs().get(currentFileInOurCommit.get(0).getItemData().getId()).getContent();
                        if (!currentFileInTheirsCommit.isEmpty()) {
                            theirFDContent = m_ActiveRepository.getBlobs().get(currentFileInTheirsCommit.get(0).getItemData().getId()).getContent();
                        }
                        conflict = new MergeConflict(fd.getFullPath(), ourFDContent, theirFDContent, m_ActiveRepository.getBlobs().get(fd.getItemData().getId()).getContent());
                        conflictedList.add(conflict);
                    }
                }
            }
        }

        addSonAddedFiles(i_OurClassifiedFiles, i_OurFileDetails, mergeList);
        addSonAddedFiles(i_TheirsClassifiedFiles, i_TheirsFileDetails, mergeList);
        //span merge list in wc
        spanWCByMergeList(mergeList);
        return conflictedList;
    }

    private void addSonAddedFiles(Map<String, FileStatusCompareAncestor> i_Son, List<FileFullPathAndItemData> i_SonFilesList, List<FileFullPathAndItemData> i_MergeList) {
        for (FileFullPathAndItemData fd : i_SonFilesList) {
            if (i_Son.get(fd.getFullPath()) == FileStatusCompareAncestor.ADDED) {
                i_MergeList.add(fd);
            }
        }
    }

    private void spanWCByMergeList(List<FileFullPathAndItemData> i_MergeList) throws IOException {
        deleteWCFromFileSystem();

        for (FileFullPathAndItemData fd : i_MergeList) {
            String content = m_ActiveRepository.getBlobs().get(fd.getItemData().getId()).getContent();
            FileUtilities.createFoldersByPathAndWriteContent(content, fd.getFullPath());
        }
    }

    public void spanWCSolvedConflictList(List<MergeConflict> i_ConflictList) throws IOException {
        for (MergeConflict conflict : i_ConflictList) {
            if (conflict.getResolveContent() != null) {
                FileUtilities.createFoldersByPathAndWriteContent(conflict.getResolveContent(), conflict.getPath());
            }
        }
    }

    public boolean isEmptyActiveRepository() {
        boolean isEmptyRepository = true;

        if (checkIfThereIsALoadedRepository()) {
            if (m_ActiveRepository.getCommits().size() > 0) {
                isEmptyRepository = false;
            }
        }

        return isEmptyRepository;
    }

    public boolean isSha1OfReachableCommit(String i_Sha1) {
        return m_ActiveRepository.getCommits().containsKey(i_Sha1);
    }

    public String getDifferenceBetweenTwoCommits(String i_CurrentCommitId, String i_parentCommitId) {
        String result = "";

        List<Folder.ItemData> deletedFilesItems = new ArrayList<>();
        List<Folder.ItemData> addedFilesItems = new ArrayList<>();
        List<Folder.ItemData> changedFilesItems = new ArrayList<>();

        if (m_ActiveRepository.getCommits().containsKey(i_CurrentCommitId) && m_ActiveRepository.getCommits().containsKey(i_parentCommitId)) {
            compareTwoCommits(i_CurrentCommitId, i_parentCommitId, deletedFilesItems, addedFilesItems, changedFilesItems);
            result += "Deleted files:\n" + getStringInfoFromItemDataList(deletedFilesItems);
            result += "Added files:\n" + getStringInfoFromItemDataList(addedFilesItems);
            result += "Changed files:\n" + getStringInfoFromItemDataList(changedFilesItems);
        }

        return result;
    }

    public void compareTwoCommits(String i_currentCommitId, String i_parentCommitId, List<Folder.ItemData> i_DeletedFilesItems,
                                  List<Folder.ItemData> i_AddedFilesItems, List<Folder.ItemData> i_ChangedFilesItems) {
        List<FileFullPathAndItemData> currentCommitDetails = getCommitBlobsAndFoldersDetails(i_currentCommitId);
        List<FileFullPathAndItemData> otherCommitDetails = getCommitBlobsAndFoldersDetails(i_parentCommitId);

        for (FileFullPathAndItemData fileDetails : currentCommitDetails) {
            List<FileFullPathAndItemData> sameFiles = otherCommitDetails.stream().filter(v -> v.getFullPath().equals(fileDetails.getFullPath())).collect(Collectors.toList());
            if (sameFiles.isEmpty()) {
                i_AddedFilesItems.add(fileDetails.getItemData());
            } else {
                if (!sameFiles.get(0).getItemData().getId().equals(fileDetails.getItemData().getId())) {
                    i_ChangedFilesItems.add(fileDetails.getItemData());
                }
                otherCommitDetails.remove(sameFiles.get(0));
            }
        }

        for (FileFullPathAndItemData fileDetailsInOtherList : otherCommitDetails) {
            i_DeletedFilesItems.add(fileDetailsInOtherList.getItemData());
        }
    }

    public void cloneRepository(String i_LocalRepoLocation, String i_RemoteRepoLocation, String i_LocalRepoName) throws IOException {
        File rootFolderInRR = new File(i_RemoteRepoLocation);
        File rootFolderInLR = new File(i_LocalRepoLocation);

        FileUtilities.copyDirectory(rootFolderInRR, rootFolderInLR);
        File nameOfRRFile = new File(rootFolderInRR.getPath() + "//.magit//repositoryName.txt");
        BufferedReader br = new BufferedReader(new FileReader(nameOfRRFile));
        String RRName = br.readLine();
        br.close();
        moveBranchesToInnerDirectory(RRName, rootFolderInLR);
        createHeadRTB(i_LocalRepoLocation, RRName);
        FileUtilities.writeToFile(Paths.get(rootFolderInLR.getPath(),
                ".magit", "repositoryName.txt").toString(),
                i_LocalRepoName);
        FileUtilities.writeToFile(Paths.get(i_LocalRepoLocation,
                ".magit", "RRLocation.txt").toString(),
                i_RemoteRepoLocation);
        //TODO: check if there is a need to switch for repo clone
        spreadHeadBranchInFileSystemToOurObjects(i_LocalRepoLocation);
    }

    private void moveBranchesToInnerDirectory(String i_RRName, File i_RootFolderInLR) throws IOException {
        File newDirectoryForBranches = new File(i_RootFolderInLR + "//.magit//branches//" + i_RRName);
        if (newDirectoryForBranches.mkdir()) {
            File[] branches = new File(i_RootFolderInLR + "//.magit//branches").listFiles();
            if (branches != null) {
                for (File file : branches) {
                    if (!file.isDirectory()) {
                        if (!file.getName().toLowerCase().equals("head.txt")) {
                            Files.copy(Paths.get(file.getPath()), Paths.get(i_RootFolderInLR + "//.magit//branches//" + i_RRName + "\\" + file.getName()));
                            file.delete();
                        }
                    }
                }
            }
        }
    }

    private void createHeadRTB(String i_LRLocation, String i_RRName) throws IOException {
        File file;
        BufferedReader br;

        file = new File(i_LRLocation + "//.magit//branches//Head.txt");
        br = new BufferedReader(new FileReader(file));
        String activeBranchName = br.readLine();
        br.close();
        file = new File(i_LRLocation + "//.magit//branches//" + i_RRName + "\\" + activeBranchName + ".txt");
        br = new BufferedReader(new FileReader(file));
        String pointedCommitId = br.readLine();
        br.close();
        FileUtilities.writeToFile(i_LRLocation + "//.magit//branches//" + activeBranchName + ".txt", pointedCommitId);
    }

    public void fetchRRNewData(String i_RRPath) throws IOException {
        Repository remoteRepository = createRepositoryFromFileSystemToObject(i_RRPath);

        String objectsPath = Paths.get(m_ActiveRepository.getLocation(),
                ".magit", "objects").toString();
        loadBlobsFilesFromRRToLR(remoteRepository.getBlobs(), m_ActiveRepository.getBlobs(), objectsPath);
        loadFoldersFilesFromRRToLR(remoteRepository.getFolders(), m_ActiveRepository.getFolders(), objectsPath);
        loadCommitsFilesFromRRToLR(remoteRepository.getCommits(), m_ActiveRepository.getCommits(), objectsPath);

        Map<String, Branch> branchMapInRR = remoteRepository.getBranches();
        Map<String, Branch> branchMapInLR = m_ActiveRepository.getBranches();

        for (Map.Entry<String, Branch> branch : branchMapInRR.entrySet()) {
            if (!branchMapInLR.containsKey(remoteRepository.getName() + "\\" + branch.getValue().getName())) {
                Branch rb = new Branch(remoteRepository.getName() + "\\" + branch.getValue().getName(), branch.getValue().getPointedCommitId());
                branchMapInLR.put(rb.getName(), rb);
            }
            FileUtilities.writeToFile(Paths.get(m_ActiveRepository.getLocation(),
                    ".magit", "branches", remoteRepository.getName(), branch.getValue().getName().concat(".txt")).toString(),
                    branch.getValue().getPointedCommitId());
        }
        spreadHeadBranchInFileSystemToOurObjects(m_ActiveRepository.getLocation());
        Factory.createFileSystem(m_ActiveRepository);
    }

    private void loadBlobsFilesFromRRToLR(Map<String, Blob> i_BlobMapInRR, Map<String, Blob> i_BlobMapInLR, String i_Path) throws IOException {
        for (Map.Entry<String, Blob> blob : i_BlobMapInRR.entrySet()) {
            if (!i_BlobMapInLR.containsKey(blob.getKey())) {
                i_BlobMapInLR.put(blob.getKey(), blob.getValue());
                FileUtilities.zip(blob.getKey(), blob.getValue().getContent(), Paths.get(i_Path, blob.getKey()).toString());
            }
        }
    }

    private void loadFoldersFilesFromRRToLR(Map<String, Folder> i_FolderMapInRR, Map<String, Folder> i_FolderMapInLR, String i_Path) throws IOException {
        for (Map.Entry<String, Folder> folder : i_FolderMapInRR.entrySet()) {
            if (!i_FolderMapInLR.containsKey(folder.getKey())) {
                i_FolderMapInLR.put(folder.getKey(), folder.getValue());
                FileUtilities.zip(folder.getKey(), folder.getValue().toString(), Paths.get(i_Path, folder.getKey()).toString());

            }
        }
    }

    private void loadCommitsFilesFromRRToLR(Map<String, Commit> i_BranchMapInRR, Map<String, Commit> i_BranchMapInLR, String i_Path) throws IOException {
        for (Map.Entry<String, Commit> commit : i_BranchMapInRR.entrySet()) {
            if (!i_BranchMapInLR.containsKey(commit.getKey())) {
                i_BranchMapInLR.put(commit.getKey(), commit.getValue());
                FileUtilities.zip(commit.getKey(), commit.getValue().toString(), Paths.get(i_Path, commit.getKey()).toString());
            }
        }
    }

    public void Pull() throws IOException {
        Branch headBranchInLR = m_ActiveRepository.getHeadBranch();
        Repository remoteRepository = createRepositoryFromFileSystemToObject(m_ActiveRepository.getRemoteRepositoryLocation());
        Branch branchInRR = remoteRepository.getBranches().get(headBranchInLR.getName());
        String commitSha1 = branchInRR.getPointedCommitId();
        addAllBranchData(commitSha1,remoteRepository, m_ActiveRepository);
        resetHeadBranch(commitSha1);
        m_ActiveRepository.getBranches().get(headBranchInLR.getTrackingAfter()).setPointedCommitId(commitSha1);
        Factory.createFileSystem(m_ActiveRepository);
    }

    private void addAllBranchData(String i_CommitSha1, Repository i_DataSupplierRepo, Repository i_ReceivingDataRepo) throws IOException {
        if (!i_ReceivingDataRepo.getCommits().containsKey(i_CommitSha1)) {
            spreadCommitsInFileSystemToOurObjectsRec(i_DataSupplierRepo.getLocation(),i_CommitSha1, i_ReceivingDataRepo);
        }
    }

    public Boolean checkIfThereAreLocalChangesOnRTB(){
        Branch activeBranch = m_ActiveRepository.getHeadBranch();
            return (!activeBranch.getPointedCommitId().equals(m_ActiveRepository.getBranches()
                    .get(activeBranch.getTrackingAfter()).getPointedCommitId()));
    }

    public void createRemoteTrackingBranchAndCheckout(String i_BranchNameToCreate, String i_PointedCommitSha1) throws IOException{
        String remoteTrackingBranchName = i_BranchNameToCreate.split("\\\\")[1];
        createRemoteTrackingBranchInFileSystemAndInOurObjects(remoteTrackingBranchName, i_PointedCommitSha1, i_BranchNameToCreate);
        checkoutBranch(remoteTrackingBranchName);
    }

    public boolean isRemoteBranch(String i_BranchName) {

        if(m_ActiveRepository.getRemoteRepositoryLocation() == null  || m_ActiveRepository.getRemoteRepositoryLocation().isEmpty()
                || !m_ActiveRepository.getBranches().containsKey(i_BranchName)){
            return false;
        }
        else{
           return(m_ActiveRepository.getBranches().get(i_BranchName).getIsRemote());
        }
    }

    public void Push() throws IOException{
        Repository remoteRepository = createRepositoryFromFileSystemToObject(m_ActiveRepository.getRemoteRepositoryLocation());
        String commitSha1 = m_ActiveRepository.getHeadBranch().getPointedCommitId();
        addAllBranchData(commitSha1,m_ActiveRepository, remoteRepository);
        remoteRepository.getHeadBranch().setPointedCommitId(commitSha1);
        Factory.createFileSystem(remoteRepository);
    }

    public Boolean checkIfRRHasNoOpenChanges() throws IOException {
        Boolean isRRWcClean ;
        String RRPath = m_ActiveRepository.getRemoteRepositoryLocation();
        String LRPath = m_ActiveRepository.getLocation();
        spreadHeadBranchInFileSystemToOurObjects(RRPath);
        isRRWcClean = isWCClean();
        spreadHeadBranchInFileSystemToOurObjects(LRPath);

        return isRRWcClean;
    }

    public Boolean checkIfBranchesPointingTheSameCommits() throws IOException {
        String branchesPath = "//.magit//branches//";
        String remoteRepositoryPath =  m_ActiveRepository.getRemoteRepositoryLocation();
        Repository RemoteRepository = createRepositoryFromFileSystemToObject(remoteRepositoryPath);
        String remoteRepositoryCommitToCompare = RemoteRepository.getHeadBranch().getPointedCommitId();

        String localRepositoryActiveBranchName = m_ActiveRepository.getHeadBranch().getName();
        //String localRepositoryRemoteBranchName = RemoteRepository.getName() + "//" + localRepositoryActiveBranch;
        String LRHeadBranchPath = m_ActiveRepository.getLocation() + branchesPath  +
                m_ActiveRepository.getRemoteRepositoryName() + "//" + localRepositoryActiveBranchName + ".txt";
        File file = new File(LRHeadBranchPath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String LRHeadBranchPointingCommitId = br.readLine();

        return LRHeadBranchPointingCommitId.equals(remoteRepositoryCommitToCompare);
    }

    private Folder.ItemData getItemDataWithThisSha1(String i_Sha1){
        Folder.ItemData itemDataWithThisSha1 = null;

        for (Map.Entry<String, Folder> entry : m_ActiveRepository.getFolders().entrySet()) {
            for(Folder.ItemData itemData : entry.getValue().getItems()){
                if(itemData.getId().equals(i_Sha1)){
                    itemDataWithThisSha1 = itemData;
                    break;
                }
            }
        }

        return itemDataWithThisSha1;
    }

    public String extractRepositoryNameFromPath(String i_RepositoryFullPath){
        String name = i_RepositoryFullPath;
        int indexName = name.lastIndexOf("/");
        if(indexName == -1){
            indexName = name.lastIndexOf("\\");
        }
        return name.substring(indexName + 1);
    }

    public void spreadLostCommitsInFileSystemToOurObjects() throws IOException{
        String objectsPath = "//.magit//objects//";

        File file = new File(m_ActiveRepository.getLocation() + objectsPath);
        File[] files = file.listFiles();
        BufferedReader br;

        for(File objectFile: files){
            String content = FileUtilities.unZip(objectFile.getAbsolutePath());
            String[] splittedContent = content.split(";");
            if(splittedContent.length == 6)
            {
                String commitSha1 = objectFile.getName();
                // lost commit
                if(!m_ActiveRepository.getCommits().containsKey(commitSha1)) {
                    String rootFolderId = splittedContent[0];
                    String precedingCommitId1 = splittedContent[1];
                    String precedingCommitId2 = splittedContent[2];
                    String message = splittedContent[3];
                    String author = splittedContent[4];
                    String dateOfCreation = splittedContent[5].trim();

                    m_ActiveRepository.getCommits().put(commitSha1, Factory.createCommit(rootFolderId, precedingCommitId1, precedingCommitId2, message, author, dateOfCreation));
                    if (!m_ActiveRepository.getFolders().containsKey(rootFolderId)) {
                        m_ActiveRepository.getFolders().put(rootFolderId, Factory.createFolder(true));
                        spreadFolderInFileSystemToOurObjectsRec(m_ActiveRepository.getFolders().get(rootFolderId), m_ActiveRepository.getLocation(), rootFolderId, m_ActiveRepository);
                    }

                    if (!precedingCommitId1.isEmpty() && !precedingCommitId1.equals("null")) {
                        spreadCommitsInFileSystemToOurObjectsRec(m_ActiveRepository.getLocation(), precedingCommitId1, m_ActiveRepository);
                    }
                    if (!precedingCommitId2.isEmpty() && !precedingCommitId2.equals("null")) {
                        spreadCommitsInFileSystemToOurObjectsRec(m_ActiveRepository.getLocation(), precedingCommitId2, m_ActiveRepository);
                    }
                }
            }
        }
    }

    public Map<String,Commit> getAllCommitsInFileSystem() throws IOException {
        spreadLostCommitsInFileSystemToOurObjects();
        return m_ActiveRepository.getCommits();
    }
}