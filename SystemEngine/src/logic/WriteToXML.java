package logic;

import jaxb.schema.generated.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WriteToXML {
    private final static String JAXB_XML_MAGIT_PACKAGE_NAME = "jaxb.schema.generated";
    private final static int SHA1_LENGTH = 40;
    private Repository m_Repository;

    public WriteToXML(Repository i_Repository) {
        m_Repository = i_Repository;
    }

    public void exportRepositoryToXml(String i_XmlPath) {
        MagitRepository magitRepository = new MagitRepository();
        magitRepository.setLocation(m_Repository.getLocation());
        magitRepository.setName(m_Repository.getName());

        if (m_Repository.getBranches().size() > 0) {
            createMagitBranches(magitRepository);
        }

        if (m_Repository.getCommits().size() > 0) {
            createMagitCommits(magitRepository);
        }

        magitRepositoryToXml(magitRepository, i_XmlPath);
    }

    private void createMagitBranches(MagitRepository i_MagitRepository) {
        Map<String, Branch> branches = m_Repository.getBranches();
        i_MagitRepository.setMagitBranches(new MagitBranches());
        List<MagitSingleBranch> magitBranches = i_MagitRepository.getMagitBranches().getMagitSingleBranch();

        for (Map.Entry<String, Branch> branchEntry : branches.entrySet()) {
            MagitSingleBranch magitBranch = new MagitSingleBranch();
            MagitSingleBranch.PointedCommit pointedCommit = new MagitSingleBranch.PointedCommit();

            pointedCommit.setId(branchEntry.getValue().getPointedCommitId());
            magitBranch.setPointedCommit(pointedCommit);
            magitBranch.setTrackingAfter(branchEntry.getValue().getTrackingAfter());
            magitBranch.setTracking(branchEntry.getValue().getIsTracking());
            magitBranch.setName(branchEntry.getValue().getName());
            magitBranch.setIsRemote(branchEntry.getValue().getIsRemote());

            if (branchEntry.getValue().getIsHead()) {
                i_MagitRepository.getMagitBranches().setHead(magitBranch.getName());
            }

            magitBranches.add(magitBranch);
        }
    }

    private void createMagitCommits(MagitRepository i_MagitRepository) {
        Map<String, Commit> commits = m_Repository.getCommits();

        i_MagitRepository.setMagitCommits(new MagitCommits());
        i_MagitRepository.setMagitFolders(new MagitFolders());
        i_MagitRepository.setMagitBlobs(new MagitBlobs());
        Set<String> sha1TrackerSet = new HashSet<>();

        List<MagitSingleCommit> magitCommits = i_MagitRepository.getMagitCommits().getMagitSingleCommit();

        for (Map.Entry<String, Commit> commitEntry : commits.entrySet()) {
            if (commitEntry.getKey().length() == SHA1_LENGTH) {
                MagitSingleCommit magitCommit = new MagitSingleCommit();
                PrecedingCommits precedingCommits = new PrecedingCommits();
                List<PrecedingCommits.PrecedingCommit> magitPrecedingCommits = precedingCommits.getPrecedingCommit();

                PrecedingCommits.PrecedingCommit magitPrecedingCommit = new PrecedingCommits.PrecedingCommit();
                magitPrecedingCommit.setId(commitEntry.getValue().getFirstPrecedingCommitId());
                magitPrecedingCommits.add(magitPrecedingCommit);

                RootFolder magitRootFolder = new RootFolder();
                magitRootFolder.setId(commitEntry.getValue().getRootFolderId());
                magitCommit.setRootFolder(magitRootFolder);

                magitCommit.setPrecedingCommits(precedingCommits);
                magitCommit.setMessage(commitEntry.getValue().getMessage());
                magitCommit.setId(commitEntry.getKey());
                magitCommit.setDateOfCreation(commitEntry.getValue().getDateOfCreation());
                magitCommit.setAuthor(commitEntry.getValue().getAuthor());

                magitCommits.add(magitCommit);

                // createMagitFolders
                MagitSingleFolder magitFolder = new MagitSingleFolder();
                magitFolder.setName(null);
                magitFolder.setLastUpdater(commitEntry.getValue().getAuthor());
                magitFolder.setLastUpdateDate(commitEntry.getValue().getDateOfCreation());
                magitFolder.setIsRoot(true);
                magitFolder.setId(commitEntry.getValue().getRootFolderId());

                createMagitFolders(i_MagitRepository, magitFolder, sha1TrackerSet);
            }
        }
    }

    private void createMagitFolders(MagitRepository i_MagitRepository, MagitSingleFolder i_CurrentMagitFolder, Set<String> i_Sha1TrackerSet) {
        List<MagitSingleFolder> magitFolders = i_MagitRepository.getMagitFolders().getMagitSingleFolder();
        magitFolders.add(i_CurrentMagitFolder);
        i_Sha1TrackerSet.add(i_CurrentMagitFolder.getId());

        MagitSingleFolder.Items items = new MagitSingleFolder.Items();
        List<Item> itemsList = items.getItem();
        i_CurrentMagitFolder.setItems(items);

        Folder folder = m_Repository.getFolders().get(i_CurrentMagitFolder.getId());

        for (Folder.ItemData itemData : folder.getItems()) {
            Item item = new Item();
            item.setType(itemData.getType().toString().toLowerCase());
            item.setId(itemData.getId());
            itemsList.add(item);

            if (!i_Sha1TrackerSet.contains(itemData.getId())) {
                if (itemData.getType().equals(Folder.ItemData.eItemType.FOLDER)) {
                    MagitSingleFolder magitSubFolder = new MagitSingleFolder();
                    magitSubFolder.setId(itemData.getId());
                    magitSubFolder.setIsRoot(false);
                    magitSubFolder.setLastUpdateDate(itemData.getLastUpdateDate());
                    magitSubFolder.setLastUpdater(itemData.getLastUpdater());
                    magitSubFolder.setName(itemData.getName());

                    createMagitFolders(i_MagitRepository, magitSubFolder, i_Sha1TrackerSet);
                } else {
                    MagitBlob magitBlob = new MagitBlob();
                    magitBlob.setName(itemData.getName());
                    magitBlob.setLastUpdater(itemData.getLastUpdater());
                    magitBlob.setLastUpdateDate(itemData.getLastUpdateDate());
                    magitBlob.setId(itemData.getId());

                    createMagitBlob(i_MagitRepository, magitBlob, i_Sha1TrackerSet);
                }
            }
        }
    }

    private void createMagitBlob(MagitRepository i_MagitRepository, MagitBlob i_MagitBlob, Set<String> i_Sha1TrackerSet) {
        List<MagitBlob> magitBlobs = i_MagitRepository.getMagitBlobs().getMagitBlob();
        String blobContent = m_Repository.getBlobs().get(i_MagitBlob.getId()).getContent();

        i_MagitBlob.setContent(blobContent);
        magitBlobs.add(i_MagitBlob);
        i_Sha1TrackerSet.add(i_MagitBlob.getId());
    }

    private void magitRepositoryToXml(MagitRepository i_MagitRepo, String i_XmlPath) {
        try {
            File file = new File(i_XmlPath);
            JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_XML_MAGIT_PACKAGE_NAME);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(i_MagitRepo, file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
