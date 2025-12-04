package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.*;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.proyect.toolrent.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoanService {
    private final EmployeeService employeeService;
    LoanRepository loanRepository;
    ClientService clientService;
    LoanXToolItemService loanXToolItemService;
    ValidationService validationService;
    ToolTypeService toolTypeService;
    ToolItemService toolItemService;
    KardexService kardexService;
    SystemConfigurationService sysConfigService;

    @Autowired
    public LoanService(LoanRepository loanRepository, ClientService clientService, LoanXToolItemService loanXToolItemService, ToolTypeService toolTypeService, ToolItemService toolItemService, KardexService kardexService, ValidationService validationService, SystemConfigurationService sysConfigService, EmployeeService employeeService) {
        this.loanRepository = loanRepository;
        this.clientService = clientService;
        this.loanXToolItemService = loanXToolItemService;
        this.toolTypeService = toolTypeService;
        this.toolItemService = toolItemService;
        this.kardexService = kardexService;
        this.validationService = validationService;
        this.sysConfigService = sysConfigService;
        this.employeeService = employeeService;
    }

    public List<LoanEntity> getAllLoans(){
        return loanRepository.findAllWithDetails();
    }

    public Optional<LoanEntity> getLoanById(Long id){
        return loanRepository.findByIdWithDetails(id);
    }

    public List<LoanEntity> getActiveLoansByClient(String clientRun){
        return loanRepository.findActiveLoansByClient(clientRun);
    }

    public List<LoanEntity> getLoanByStatus(String status){
        return loanRepository.findByStatusWithDetails(status);
    }

    public List<LoanEntity> getLoanByValidity(String validity){
        return loanRepository.findByValidity(validity);
    }

    public List<Map<String, Object>> getMostLoanedTools(LocalDate startDate, LocalDate endDate){
        List<Object[]> results = loanXToolItemService.getMostLoanedToolsBetweenDates(startDate, endDate);

        //The shape of the object is changed to display the name of the tool and the number of loans
        List<Map<String, Object>> loanedToolList = results.stream()
                .map(r -> Map.of(
                        "toolName", ((ToolItemEntity) r[0]).getToolType().getName(),
                        "totalLoans", r[1]
                ))
                .toList();

        //The most borrowed tool(s) is/are sought
        List<Map<String, Object>> mostLoanedTools = new ArrayList<>();
        long maxLoans = 0L;

        for(Map<String, Object> mapTool : loanedToolList){
            Long totalLoans = (Long) mapTool.get("totalLoans");

            if(totalLoans > maxLoans){
                //If a tool is found that has more loans
                maxLoans = totalLoans;
                mostLoanedTools.clear();
                mostLoanedTools.add(mapTool);

            } else if(totalLoans == maxLoans){
                //If the tool has the same number of loans as the current one
                mostLoanedTools.add(mapTool);
            }
            //If totalLoans < maxLoans it does nothing
        }
        return mostLoanedTools;
    }

    public boolean exists(Long id){
        return loanRepository.existsById(id);
    }

    @Transactional
    public LoanEntity createLoan(LoanEntity loan, String employeeRun){
        //The employee is searched in the database
        EmployeeEntity dbEmployeeEnt = employeeService.getEmployeeByRun(employeeRun)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        //EmployeeEntity dbEmployeeEnt = dbEmployee.get();

        //The client is searched in the database
        ClientEntity dbClientEnt = clientService.getClientByRun(loan.getClient().getRun())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        //ClientEntity dbClientEnt = dbClient.get();

        //Save the loan tools list
        List<LoanXToolItemEntity> toolItemsFromFront = loan.getLoanTools();

        if (toolItemsFromFront == null || toolItemsFromFront.isEmpty()) {
            throw new RuntimeException("La lista de herramientas no puede estar vacía.");
        }

        //Check that the client doesn't have a debt
        if(dbClientEnt.getStatus().equals("Restringido") || dbClientEnt.getDebt() > 0){
            throw new RuntimeException("El cliente no puede arrendar debido a deudas impagas");
        }

        //Check that the client doesn't have (or will have) 5 borrowed tools
        if(dbClientEnt.getActiveLoans() >= 5){
            throw new RuntimeException("El cliente no puede tener más de 5 arriendos activos");
        }

        //Check that the client doesn't have any overdue loans
        List<LoanEntity> overdueLoans = loanRepository.findOverdueLoans(dbClientEnt.getRun(), LocalDate.now());
        if(!overdueLoans.isEmpty()){
            throw new RuntimeException("El cliente tiene préstamos atrasados pendientes. No puede arrendar nuevo.");
        }

        //The set of Ids is obtained directly form the database
        Set<Long> rentedToolsIds = loanXToolItemService.getActiveLoansToolTypeIdsByClient(dbClientEnt.getRun());

        List<LoanXToolItemEntity> toolItemsToSave = new ArrayList<>();

        //It's verified whether the new tools have already been leased by the client
        for (LoanXToolItemEntity toolItemRequest : toolItemsFromFront) {
            ToolItemEntity dbToolItem = toolItemService.getToolItemById(toolItemRequest.getToolItem().getId())
                    .orElseThrow(() -> new RuntimeException("Herramienta no encontrada en la base de datos"));

            //The stock it's validated
            if (dbToolItem.getStatus() != ToolStatus.DISPONIBLE || dbToolItem.getToolType().getAvailableStock() < 1) {
                throw new RuntimeException("La herramienta " + dbToolItem.getSerialNumber() + " no está disponible.");
            }

            Long typeId = dbToolItem.getToolType().getId();
            if(rentedToolsIds.contains(typeId)){
                throw new RuntimeException("El cliente ya tiene arrendada una herramienta de tipo: " + dbToolItem.getToolType().getName());
            }

            //The relationship object is prepared for later storage
            toolItemRequest.setToolItem(dbToolItem);
            toolItemsToSave.add(toolItemRequest);
        }

        //Check if there is a fee coming from the frontend
        if (loan.getLateReturnFee() == null) {
            loan.setLateReturnFee(sysConfigService.getLateReturnFee());
        }

        //Check that the return date isn't before the loan date
        if(loan.getReturnDate().isBefore(loan.getLoanDate())){
            throw new RuntimeException("La fecha de devolución es previa a la de entrega");
        }

        //If all tools have stock and the client doesn't have or hasn't exceeded the tool limit then save the loan
        loan.setClient(dbClientEnt);
        loan.setEmployee(dbEmployeeEnt);
        LoanEntity savedLoan = loanRepository.save(loan);

        //Create and save the associated kardex for each tool, reduce the tool stock and save the relationships
        for(LoanXToolItemEntity loanToolItem : toolItemsToSave){
            //Link to the saved loan
            loanToolItem.setLoan(savedLoan);
            loanXToolItemService.save(loanToolItem);

            //Update stock and kardex
            ToolItemEntity toolItem = loanToolItem.getToolItem();
            toolItem.setStatus(ToolStatus.PRESTADA);
            toolItemService.updateToolItem(toolItem.getId(), toolItem);

            toolTypeService.decreaseAvailableStock(toolItem.getToolType(), 1);

            kardexService.createLoanKardex(toolItem.getToolType());
        }

        //Update the client borrowed tools
        dbClientEnt.setActiveLoans(dbClientEnt.getActiveLoans() + 1);
        clientService.saveClient(dbClientEnt);

        return savedLoan;
    }

    @Transactional
    public LoanEntity returnLoan(Long id, LoanEntity loan){
        //The loan is searched in the database
        Optional<LoanEntity> dbLoan = getLoanById(id);
        LoanEntity dbLoanEnt = dbLoan.get();

        //The client is searched in the database
        Optional<ClientEntity> dbClient = clientService.getClientByRun(loan.getClient().getRun());
        ClientEntity dbClientEnt = dbClient.get();

        //Update stock and validity of the tool
        List<LoanXToolItemEntity> dbLoanToolItems = loanXToolItemService.getLoanToolItemsWithToolTypesByLoan_Id(id);

        //The list of tools from the frontend is converted into a map for quick searching
        Map<Long, ToolItemEntity> toolItemMap = new HashMap<>();
        if(loan.getLoanTools() != null){
            for(LoanXToolItemEntity loanToolItems : loan.getLoanTools()){
                toolItemMap.put(loanToolItems.getToolItem().getId(), loanToolItems.getToolItem());
            }
        }

        //The database is browsed
        for(LoanXToolItemEntity dbLoanToolItem : dbLoanToolItems) {
            ToolItemEntity dbToolItem = dbLoanToolItem.getToolItem();

            ToolItemEntity toolItemMapInfo = toolItemMap.get(dbToolItem.getId());

            if (toolItemMapInfo.getDamageLevel() == ToolDamageLevel.EN_EVALUACION) {
                toolItemMapInfo.setStatus(ToolStatus.EN_REPARACION);
                toolItemService.updateToolItem(dbToolItem.getId(), toolItemMapInfo);

                kardexService.createDisableToolItemKardex(dbToolItem);

                //Since the tool returned by the client is damaged, the client is prevented from requesting more tools until the damage level has been verified
                //If the tools were indeed in good condition, they will be "Active" again
                dbClientEnt.setStatus("Restringido");

            } else if(toolItemMapInfo.getDamageLevel() == ToolDamageLevel.NO_DANADA){
                //"EN_EVALUACION" it's a placeholder until the tool damage is evaluated
                //If it isn't in under review then it's "No dañada"
                toolItemMapInfo.setStatus(ToolStatus.DISPONIBLE);
                toolItemService.updateToolItem(dbToolItem.getId(), toolItemMapInfo);
                toolTypeService.increaseAvailableStock(dbToolItem.getToolType(), 1);

                kardexService.createReturnKardex(dbToolItem.getToolType());

            } else{
                throw new RuntimeException("La herramienta ya tiene un nivel de daño");
            }
        }

        //Decrease the client's active loans count
        dbClientEnt.setActiveLoans(dbClientEnt.getActiveLoans() - 1);
        clientService.saveClient(dbClientEnt);

        //If the client returned the tools late
        if(dbLoanEnt.getReturnDate().isBefore(LocalDate.now())){
            //Late return fee calculation and change of status to the client
            int daysBetween = (int) ChronoUnit.DAYS.between(dbLoanEnt.getReturnDate(), LocalDate.now());
            Integer lateReturnFee = daysBetween * dbLoanEnt.getLateReturnFee();

            clientService.chargeClientLateReturnFee(dbClientEnt, lateReturnFee);

            dbLoanEnt.setValidity("Atrasado");
        } else{
            dbLoanEnt.setValidity("Puntual");
        }

        dbLoanEnt.setReturnTime(LocalTime.now());
        dbLoanEnt.setStatus("Finalizado");
        return loanRepository.save(dbLoanEnt);
    }

    public LoanEntity updateLateReturnFee(Long id, LoanEntity loan){
        //The loan is searched in the database
        Optional<LoanEntity> dbLoan = loanRepository.findById(id);
        LoanEntity dbLoanEnt = dbLoan.get();

        //Since only the late return fine can be updated, only that is reviewed
        if(loan.getLateReturnFee() != null){
            if(!validationService.isValidNumber(loan.getLateReturnFee())){
                throw new IllegalArgumentException("Monto de multa por atraso incorrecta");
            }
            dbLoanEnt.setLateReturnFee(loan.getLateReturnFee());
        }

        return loanRepository.save(dbLoanEnt);
    }

    @Transactional
    public void checkAndSetLateStatuses() {
        List<LoanEntity> activeLoans = loanRepository.findByStatusWithDetails("Activo");

        for (LoanEntity loan : activeLoans) {
            if (loan.getReturnDate().isBefore(LocalDate.now())) {

                //Update the validity to 'Atrasado'
                loan.setValidity("Atrasado");
                loanRepository.save(loan);

                //Restrict the client if it isn't
                ClientEntity client = loan.getClient();
                if (!client.getStatus().equals("Restringido")) {
                    client.setStatus("Restringido");
                    clientService.saveClient(client);
                }
            }
        }
    }
}