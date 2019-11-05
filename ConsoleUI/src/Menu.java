import logic.FileUtilities;
import logic.Magit;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private final static int SHA1_LENGTH = 40;
    private final Magit m_Magit = new Magit();

    public enum eMenuOption {
        CHANGE_USER_NAME, LOAD_REPOSITORY_FROM_XML, SWITCH_REPOSITORY, SHOW_CURRENT_COMMIT_FILES_SYSTEM_INFORMATION,
        SHOW_WORKING_COPY_STATUS, COMMIT, LIST_AVAILABLE_BRANCHES, CREATE_NEW_BRANCH, DELETE_BRANCH,
        CHECKOUT_BRANCH, SHOW_CURRENT_BRANCH_HISTORY, ADD_NEW_REPOSITORY, RESET_HEAD_BRANCH, EXPORT_REPOSITORY_TO_XML, EXIT
    }

    public void run() {
        boolean isExit;

        do {
            printMenuTitle();
            printMenu();
            isExit = getInputFromUser();
        } while (!isExit);
        System.out.println("Good bye!");
    }

    private void printMenuTitle() {
        String currentRepositoryLocation;

        if (m_Magit.getActiveRepository() == null) {
            currentRepositoryLocation = "N/A";
        } else {
            currentRepositoryLocation = m_Magit.getActiveRepository().getLocation();
        }

        System.out.println("Magit Menu      Current User: " + m_Magit.getActiveUserName() + "     Current repository location: " + currentRepositoryLocation);
    }

    private void printMenu() {
        System.out.println("0. Change user name" + System.lineSeparator()
                + "1. Load repository from XML" + System.lineSeparator()
                + "2. Switch repository" + System.lineSeparator()
                + "3. Show current commit file system information" + System.lineSeparator()
                + "4. Show working copy status" + System.lineSeparator()
                + "5. Commit" + System.lineSeparator()
                + "6. List available branches" + System.lineSeparator()
                + "7. Create new branch" + System.lineSeparator()
                + "8. Delete branch" + System.lineSeparator()
                + "9. Checkout branch" + System.lineSeparator()
                + "10. Show current branch history" + System.lineSeparator()
                + "11. Add new repository" + System.lineSeparator()
                + "12. Reset head branch" + System.lineSeparator()
                + "13. Export repository to XML" + System.lineSeparator()
                + "14. Exit");
    }

    private boolean getInputFromUser() {
        int userIntegerInput = 0;
        boolean validInput = false;
        Scanner scanner = new Scanner(System.in);

        do {
            try {
                userIntegerInput = Integer.parseInt(scanner.nextLine());
                if (userIntegerInput >= 0 && userIntegerInput < eMenuOption.values().length) {
                    validInput = true;
                } else {
                    System.out.println("Invalid input! Your choice is out of range, try again:");
                }
            } catch (Exception exception) {
                System.out.println("Invalid input! Please enter a number:");
            }
        } while (!validInput);

        return activeUserSelection(eMenuOption.values()[userIntegerInput]);
    }

    private boolean activeUserSelection(eMenuOption i_UserSelection) {
        boolean isExit = false;

        switch (i_UserSelection) {
            case CHANGE_USER_NAME:
                changeUserName();
                break;
            case LOAD_REPOSITORY_FROM_XML:
                loadRepositoryFromXML();
                break;
            case SWITCH_REPOSITORY:
                switchRepository();
                break;
            case SHOW_CURRENT_COMMIT_FILES_SYSTEM_INFORMATION:
                showCurrentCommitFileSystemInformation();
                break;
            case SHOW_WORKING_COPY_STATUS:
                showWorkingCopyStatus();
                break;
            case COMMIT:
                commit();
                break;
            case LIST_AVAILABLE_BRANCHES:
                listAvailableBranches();
                break;
            case CREATE_NEW_BRANCH:
                createNewBranch();
                break;
            case DELETE_BRANCH:
                deleteBranch();
                break;
            case CHECKOUT_BRANCH:
                checkoutBranch();
                break;
            case SHOW_CURRENT_BRANCH_HISTORY:
                showCurrentBranchHistory();
                break;
            case ADD_NEW_REPOSITORY:
                addNewRepository();
                break;
            case RESET_HEAD_BRANCH:
                resetHeadBranch();
                break;
            case EXPORT_REPOSITORY_TO_XML:
                exportRepositoryToXML();
                break;
            case EXIT:
                isExit = true;
                break;
        }

        return isExit;
    }

    private void changeUserName() {
        String newUserName = getStringFromUser("Please enter a user name:",
                "Invalid input! You can't enter empty name, try again:");

        m_Magit.setActiveUserName(newUserName);
    }

    private boolean isExistingRepositoryNeedToBeDeleted() {
        Scanner scanner = new Scanner(System.in);
        String userStringInput;
        boolean isValidInput = false;

        System.out.println("There is already a repository in the current location\nPlease select one of the following options:\n" +
                "1. Delete the existing repository and create a new one from the xml\n" +
                "2. Proceed with existing repository");
        do {
            userStringInput = scanner.nextLine();
            if (userStringInput.equals("1") || userStringInput.equals("2")) {
                isValidInput = true;
            } else {
                System.out.println("Invalid input! Your choice is not valid, try again:");
            }
        } while (!isValidInput);

        return userStringInput.equals("1");
    }

    private void loadRepositoryFromXML() {
        String XMLFullPath = getXMLFullPathFromUser();
        List<String> errors = new ArrayList<>();

        if (FileUtilities.isFileXMLAndExists(XMLFullPath)) {
            try {
                if (m_Magit.isXMLValid(XMLFullPath, errors)) {
                    if (m_Magit.isRepositoryFileAlreadyExists()) {
                        if (isExistingRepositoryNeedToBeDeleted()) {
                            try {
                                m_Magit.deleteRepositoryWithLocationFromXML();
                                System.out.println("The existing repository has been deleted!");
                                m_Magit.createRepositoryFromXML();
                                System.out.println("The repository has been loaded!");
                            } catch (IOException e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        } else {
                            m_Magit.spreadHeadBranchInFileSystemToOurObjects(m_Magit.getDataFromXML().getMagitRepository().getLocation());
                            System.out.println("Continues with the existing repository");
                        }
                    } else {
                        m_Magit.createRepositoryFromXML();
                        System.out.println("The repository has been loaded!");
                    }
                } else {
                    System.out.println("Invalid XML:");
                    for (String error : errors) {
                        System.out.println(error);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("File does not exist / The file is not XML");
        }
    }

    private String getXMLFullPathFromUser() {
        return getStringFromUser("Please enter the XML full path:",
                "Invalid input! You can't enter empty path, try again:");
    }

    private String getStringFromUser(String i_Message, String i_ErrorMessage) {
        Scanner scanner = new Scanner(System.in);
        String stringFromUser;

        System.out.println(i_Message);
        stringFromUser = scanner.nextLine();

        while (stringFromUser.isEmpty()) {
            System.out.println(i_ErrorMessage);
            stringFromUser = scanner.nextLine();
        }

        return stringFromUser;
    }

    private void switchRepository() {
        String repositoryFullPath = getStringFromUser("Please enter the repository full path:",
                "Invalid input! You can't enter empty path, try again:");

        if (FileUtilities.isRepositoryFileAlreadyExists(repositoryFullPath)) {
            try {
                if (m_Magit.isTheActiveRepository(repositoryFullPath)) {
                    System.out.println("This is already the active repository, nothing has changed");
                } else {
                    m_Magit.spreadHeadBranchInFileSystemToOurObjects(repositoryFullPath);
                    System.out.println("The active repository has been swapped!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("This repository does not exist!");
        }
    }

    private void showCurrentCommitFileSystemInformation() {
        if (m_Magit.checkIfThereIsALoadedRepository()) {
            if(m_Magit.checkIfThereAreCommitsInTheActiveRepository()) {
                System.out.println("Current commit file system information:");
                System.out.print(m_Magit.getCurrentCommitFileSystemInformation());
            }
            else{
                System.out.println("There are no commits in the active repository!");
            }
        } else {
            System.out.println("There is no loaded repository!");
        }
    }

    private void showCurrentBranchHistory() {
        if (m_Magit.checkIfThereIsALoadedRepository()) {
            if (m_Magit.checkIfThereAreCommitsInTheActiveRepository()) {
                try {
                    System.out.println("The current branch history:");
                    System.out.print(m_Magit.getCurrentBranchHistory());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("The are no commits in the active repository");
            }
        } else {
            System.out.println("There is no loaded repository!");
        }
    }

    private void showWorkingCopyStatus() {
        if(m_Magit.checkIfThereIsALoadedRepository()) {
            try {
                System.out.println("The working copy status:");
                System.out.print(m_Magit.getWorkingCopyStatus());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("There is no loaded repository!");
        }
    }

    private void listAvailableBranches() {
        if (m_Magit.checkIfThereIsALoadedRepository()) {
            try {
                System.out.println("Available branches:");
                System.out.print(m_Magit.getAvailableBranchesData());
            } catch (Exception e) {
                e.printStackTrace();

            }
        } else {
            System.out.println("There is no loaded repository!");
        }
    }

    private void commit() {
        if (m_Magit.checkIfThereIsALoadedRepository()) {
            try {
                if (!m_Magit.isWCClean()) {
                    String commitMessage = getStringFromUser("Please enter a message:",
                            "Invalid input! You can't enter empty message, try again:");
                    try {
                        m_Magit.commit(commitMessage);
                        System.out.println("The commit has been made!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("There are no open changes - no need to commit!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("There is no loaded repository!");
        }

    }

    private void createNewBranch() {
        String branchName;

        if (m_Magit.checkIfThereIsALoadedRepository() && m_Magit.checkIfThereAreCommitsInActiveRepository()) {
            try {
                branchName = getStringFromUser("Please enter a branch name:",
                        "Invalid input! You can't enter empty name, try again:");
                if (m_Magit.isBranchExistsInFileSystem(branchName)) {
                    System.out.println("This branch already exists");
                } else {
                    if(isThereANeedToCheckoutAfterAddingTheNewBranch())
                    {
                        m_Magit.createNewBranchInFileSystemAndInOurObjects(branchName, m_Magit.getActiveRepository().getHeadBranch().getPointedCommitId());
                        System.out.println("The branch has been created!");
                        if(m_Magit.isWCClean()){
                            m_Magit.checkoutBranch(branchName);
                            System.out.println("Checkout has been done!");
                        }
                        else{
                            System.out.println("Checkout has not been done - there are open changes in the working copy!");
                        }
                    }
                    else {
                        m_Magit.createNewBranchInFileSystemAndInOurObjects(branchName, m_Magit.getActiveRepository().getHeadBranch().getPointedCommitId());
                        System.out.println("The branch has been created!");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (!m_Magit.checkIfThereIsALoadedRepository()) {
                System.out.println("There is no loaded repository!");
            } else {
                System.out.println("There are no commits in the active repository!");
            }
        }
    }

    private void deleteBranch() {
        String branchName;

        if (m_Magit.checkIfThereIsALoadedRepository()) {
            try {
                branchName = getStringFromUser("Please enter a branch name:",
                        "Invalid input! You can't enter empty name, try again:");
                if (!m_Magit.isBranchExistsInFileSystem(branchName)) {
                    System.out.println("This branch does not exist");
                } else if (m_Magit.isHeadBranch(branchName)) {
                    System.out.println("You can not delete the head branch!");
                } else {
                    m_Magit.deleteBranch(branchName);
                    System.out.println("The branch has been deleted!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("There are no branches!");
        }
    }

    private void addNewRepository() {
        String repositoryFullPath = getStringFromUser("Please enter a full path:",
                "Invalid input! You can't enter empty path, try again:");
        String repositoryName;

        if(!FileUtilities.isFileExists(repositoryFullPath)) {
            File parentFile = new File(repositoryFullPath).getParentFile();
            if(parentFile != null) {
                String parentFullPath = parentFile.getPath();
                if(FileUtilities.isFileFolderAndExists(parentFullPath)){
                    try {
                        repositoryName = getStringFromUser("Please enter a name for the new repository:",
                                "Invalid input! You can't enter empty name, try again:");
                        m_Magit.addNewRepositoryInFileSystem(repositoryFullPath, repositoryName, true);
                        System.out.println("The repository has been created!");
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    System.out.println("The given path does not exist / The given path is not a folder");
                }
            }
            else{
                System.out.println("The given path does not exist / The given path is not a folder");
            }
        }
        else{
            System.out.println("This path already exists!");
        }
    }

    private void exportRepositoryToXML() {
        String xmlFullPath;

        if (m_Magit.checkIfThereIsALoadedRepository()) {
            xmlFullPath = getStringFromUser("Please enter the name of the xml file (as a full path):",
                    "Invalid input! You can't enter empty path, try again:");
            Path xmlPath = null;

            try {
                xmlPath = Paths.get(xmlFullPath);
            } catch (InvalidPathException ipe) {
                System.out.println("This is not a path!");
            }

            if (FileUtilities.isFileXML(xmlFullPath)) {

                m_Magit.exportRepositoryToXML(xmlFullPath);
                System.out.println("The process ended successfully!");

            } else {
                System.out.println("Your path should end with \".xml\"!");
            }
        } else {
            System.out.println("There is no loaded repository!");
        }
    }

    private void resetHeadBranch() {
        String newPointedCommitSha1;

        if (m_Magit.checkIfThereIsALoadedRepository()) {
            newPointedCommitSha1 = getStringFromUser("Please enter the new pointed commit SHA-1:",
                    "Invalid input! You can't enter empty SHA-1, try again:");

            if (newPointedCommitSha1.length() != SHA1_LENGTH) {
                System.out.println("Invalid input! SHA-1 must include 40 Hexa characters");
            } else if (!m_Magit.isSha1ExistsInFileSystem(newPointedCommitSha1)) {
                System.out.println("This SHA-1 does not exist!");
            } else if (!m_Magit.isSha1OfReachableCommit(newPointedCommitSha1)) {
                System.out.println("This is not a SHA-1 of reachable commit!");
            } else if (m_Magit.isSha1OfPointedCommitOfHeadBranch(newPointedCommitSha1)) {
                System.out.println("This is already the pointed commit SHA-1!");
            } else {
                try {
                    if (!m_Magit.isWCClean()) {
                        if (!checkIfThereAreOpenChangesInWCThatNeedToBeDeleted()) {
                            System.out.println("Please make your commit:");
                            return;
                        } else {
                            try {
                                m_Magit.resetHeadBranch(newPointedCommitSha1);
                                System.out.println("Head branch has been reset!");
                                showCurrentCommitFileSystemInformation();
                            } catch (IOException e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        }
                    } else {
                        try {
                            m_Magit.resetHeadBranch(newPointedCommitSha1);
                            System.out.println("Head branch has been reset!");
                            showCurrentCommitFileSystemInformation();
                        } catch (IOException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } else {
            System.out.println("There is no loaded repository!");
        }
    }

    private void checkoutBranch(){
        String newHeadBranchName;

        if (m_Magit.checkIfThereIsALoadedRepository()) {
            newHeadBranchName = getStringFromUser("Please enter the new head branch name:",
                    "Invalid input! You can't enter empty name, try again:");

            if (m_Magit.getActiveRepository().getHeadBranch().getName().equals(newHeadBranchName)) {
                System.out.println("You cannot checkout to the current head branch!");
            } else if (!m_Magit.isBranchExistsInFileSystem(newHeadBranchName)) {
                System.out.println("This branch does not exist!");
            } else {
                try {
                    if (!m_Magit.isWCClean()) {
                        if (!checkIfThereAreOpenChangesInWCThatNeedToBeDeleted()) {
                            System.out.println("Please make your commit:");
                            return;
                        } else {
                            try {
                                m_Magit.checkoutBranch(newHeadBranchName);
                                System.out.println("Checkout has been done!");
                            } catch (IOException e) {
                                System.out.println("Error: " + e.getMessage());
                            }
                        }
                    } else {
                        try {
                            m_Magit.checkoutBranch(newHeadBranchName);
                            System.out.println("Checkout has been done!");
                        } catch (IOException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                    }
                } catch(IOException e){
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }
        else{
            System.out.println("There is no loaded repository!");
        }
    }

    private boolean checkIfThereAreOpenChangesInWCThatNeedToBeDeleted() {
        Scanner scanner = new Scanner(System.in);
        String userStringInput;
        boolean isValidInput = false;

        System.out.println("There are open changes in the working copy\nPlease select one of the following options:\n" +
                "1. Stop and commit the open changes\n" +
                "2. Proceed with the process - open changes will be deleted");
        do {
            userStringInput = scanner.nextLine();
            if (userStringInput.equals("1") || userStringInput.equals("2")) {
                isValidInput = true;
            } else {
                System.out.println("Invalid input! Your choice is not valid, try again:");
            }
        } while (!isValidInput);

        return userStringInput.equals("2");
    }

    private boolean isThereANeedToCheckoutAfterAddingTheNewBranch() {
        Scanner scanner = new Scanner(System.in);
        String userStringInput;
        boolean isValidInput = false;

        System.out.println("Would you like to checkout to the new branch?\n1. Yes\n2. No");
        do {
            userStringInput = scanner.nextLine();
            if (userStringInput.equals("1") || userStringInput.equals("2")) {
                isValidInput = true;
            } else {
                System.out.println("Invalid input! Your choice is not valid, try again:");
            }
        } while (!isValidInput);

        return userStringInput.equals("1");
    }
}

