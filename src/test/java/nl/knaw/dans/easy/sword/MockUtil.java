package nl.knaw.dans.easy.sword;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.knaw.dans.easy.business.dataset.DatasetSubmissionImpl;
import nl.knaw.dans.easy.data.Data;
import nl.knaw.dans.easy.data.userrepo.EasyUserRepo;
import nl.knaw.dans.easy.domain.authn.Authentication.State;
import nl.knaw.dans.easy.domain.authn.UsernamePasswordAuthentication;
import nl.knaw.dans.easy.domain.dataset.DatasetImpl;
import nl.knaw.dans.easy.domain.dataset.item.FileItemVO;
import nl.knaw.dans.easy.domain.dataset.item.ItemOrder;
import nl.knaw.dans.easy.domain.dataset.item.ItemVO;
import nl.knaw.dans.easy.domain.dataset.item.filter.ItemFilters;
import nl.knaw.dans.easy.domain.exceptions.DomainException;
import nl.knaw.dans.easy.domain.exceptions.ObjectNotFoundException;
import nl.knaw.dans.easy.domain.model.Dataset;
import nl.knaw.dans.easy.domain.model.disciplinecollection.DisciplineCollection;
import nl.knaw.dans.easy.domain.model.disciplinecollection.DisciplineContainerImpl;
import nl.knaw.dans.easy.domain.model.emd.types.ApplicationSpecific.MetadataFormat;
import nl.knaw.dans.easy.domain.model.user.EasyUser;
import nl.knaw.dans.easy.domain.model.user.EasyUser.Role;
import nl.knaw.dans.easy.domain.user.EasyUserImpl;
import nl.knaw.dans.easy.domain.worker.WorkListener;
import nl.knaw.dans.easy.domain.worker.WorkReporter;
import nl.knaw.dans.easy.servicelayer.services.DatasetService;
import nl.knaw.dans.easy.servicelayer.services.ItemService;
import nl.knaw.dans.easy.servicelayer.services.Services;
import nl.knaw.dans.easy.servicelayer.services.UserService;

import org.easymock.EasyMock;

public class MockUtil
{
    protected static final String       PASSWORD        = "secret";

    protected static final String       INVALID_USER_ID = "nobody";
    protected static final String       VALID_USER_ID   = "somebody";
    protected static final String       ARCHIV_USER_ID  = "archivist";

    protected static final EasyUserImpl USER            = createSomeBody();
    protected static final EasyUserImpl ARCHIVIST       = createArchivist();

    private static int                  countDatasets   = 0;

    public void mockAll() throws Exception
    {
        mockItemService();
        mockDatasetService();
        mockUser();
    }

    @SuppressWarnings("unchecked")
    public void mockItemService() throws Exception
    {
        final ItemService itemService = EasyMock.createMock(ItemService.class);
        new Services().setItemService(itemService);

        itemService.addDirectoryContents(//
                EasyMock.isA(EasyUserImpl.class), //
                EasyMock.isA(DatasetImpl.class), //
                EasyMock.isA(String.class), //
                EasyMock.isA(File.class), //
                EasyMock.isA(List.class), //
                EasyMock.isA(WorkReporter.class));
        EasyMock.expectLastCall().anyTimes();
        
        EasyMock.expect(itemService.getFilesAndFolders(//
                EasyMock.isA(EasyUserImpl.class), //
                EasyMock.isA(DatasetImpl.class), //
                EasyMock.isA(HashSet.class))//
                ).andReturn(new ArrayList<ItemVO>()).anyTimes();

        EasyMock.expect(itemService.getFilesAndFolders(//
                EasyMock.isA(EasyUserImpl.class), //
                EasyMock.isA(DatasetImpl.class), //
                EasyMock.isA(String.class), //parent sid 
                EasyMock.isA(Integer.class),// limit
                EasyMock.isA(Integer.class),// offset
                (ItemOrder)EasyMock.isNull(),//
                (ItemFilters)EasyMock.isNull())//
                ).andReturn(new ArrayList<ItemVO>()).anyTimes();

        EasyMock.expect(itemService.getFiles(//
                EasyMock.isA(EasyUserImpl.class), //
                EasyMock.isA(DatasetImpl.class), //
                EasyMock.isA(String.class), //parent sid 
                (Integer)EasyMock.isNull(),// limit
                (Integer)EasyMock.isNull(),// offset
                (ItemOrder)EasyMock.isNull(),//
                (ItemFilters)EasyMock.isNull())).andReturn(new ArrayList<FileItemVO>() ).anyTimes();

        EasyMock.replay(itemService);
    }

    public void mockDatasetService() throws Exception
    {
        final Dataset dataset = new DatasetImpl("mock:" + (countDatasets++), MetadataFormat.SOCIOLOGY);
        final DatasetService datasetService = EasyMock.createMock(DatasetService.class);
        new Services().setDatasetService(datasetService);

        EasyMock.expect(datasetService.newDataset(MetadataFormat.SOCIOLOGY)).andReturn(dataset).anyTimes();

        datasetService.submitDataset(EasyMock.isA(DatasetSubmissionImpl.class), EasyMock.isA(WorkListener.class));
        EasyMock.expectLastCall().anyTimes();

        datasetService.submitDataset(EasyMock.isA(DatasetSubmissionImpl.class));
        EasyMock.expectLastCall().anyTimes();

        datasetService.publishDataset(//
                EasyMock.isA(EasyUser.class),//
                EasyMock.isA(DatasetImpl.class),//
                EasyMock.eq(false),//
                EasyMock.eq(true));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.replay(datasetService);
    }

    public void mockUser() throws Exception
    {
        final EasyUserRepo userRepo = EasyMock.createMock(EasyUserRepo.class);
        final UserService userService = EasyMock.createMock(UserService.class);

        new Data().setUserRepo(userRepo);
        new Services().setUserService(userService);

        EasyMock.expect(userRepo.findById(VALID_USER_ID)).andReturn(USER).anyTimes();

        EasyMock.expect(userRepo.authenticate(VALID_USER_ID, PASSWORD)).andReturn(true).anyTimes();
        EasyMock.expect(userRepo.authenticate(INVALID_USER_ID, PASSWORD)).andReturn(false).anyTimes();
        EasyMock.expect(userRepo.authenticate(null, null)).andReturn(false).anyTimes();
        EasyMock.expect(userRepo.authenticate("", "")).andReturn(false).anyTimes();

//        EasyMock.expect(userRepo.findById(VALID_USER_ID)).andReturn(USER).anyTimes();
        UsernamePasswordAuthentication value = new UsernamePasswordAuthentication(PASSWORD, VALID_USER_ID);
        value.setState(State.Authenticated);
        value.setUser(new EasyUserImpl(VALID_USER_ID));
//        userService.authenticate(EasyMock.eq(value));
        userService.authenticate(EasyMock.isA(UsernamePasswordAuthentication.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(userRepo.findById(ARCHIV_USER_ID)).andReturn(ARCHIVIST).anyTimes();
//        userService.authenticate(EasyMock.eq(new UsernamePasswordAuthentication(null, ARCHIV_USER_ID)));
//        EasyMock.expectLastCall().anyTimes();

//        userService.authenticate(EasyMock.eq(new UsernamePasswordAuthentication(null, null)));
//        EasyMock.expectLastCall().anyTimes();


        
        EasyMock.replay(userRepo, userService);
    }
    
    public static DisciplineCollection mockDisciplineCollection () {
    
        final String disciplineId = "easy-discipline:2";
        final DisciplineContainerImpl discipline = new DisciplineContainerImpl(disciplineId);
        DisciplineCollection disciplineCollection = EasyMock.createMock(DisciplineCollection.class);
        discipline.setName("Humanities");
        try
        {
            EasyMock.expect(
                    disciplineCollection.getDisciplineBySid(disciplineId)
            ).andReturn(discipline).anyTimes();
        }
        catch (ObjectNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DomainException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        EasyMock.replay(disciplineCollection);
        return disciplineCollection;
    }

    private static EasyUserImpl createSomeBody()
    {
        final EasyUserImpl user = new EasyUserImpl();
        user.setId(VALID_USER_ID);
        user.setPassword(PASSWORD);
        user.setInitials("S.");
        user.setFirstname("Some");
        user.setSurname("Body");
        user.setEmail("some@body.com");
        user.setState(EasyUser.State.ACTIVE);
        return user;
    }

    private static EasyUserImpl createArchivist()
    {
        final Set<Role> roles = new HashSet<Role>();
        roles.add(Role.ARCHIVIST);
        final EasyUserImpl archivist = new EasyUserImpl();
        archivist.setId(ARCHIV_USER_ID);
        archivist.setPassword(PASSWORD);
        archivist.setInitials("A.I.");
        archivist.setFirstname("Arch");
        archivist.setSurname("VIST");
        archivist.setEmail("arch.i.@vist.com");
        archivist.setState(EasyUser.State.ACTIVE);
        archivist.setRoles(roles);
        return archivist;
    }
}
