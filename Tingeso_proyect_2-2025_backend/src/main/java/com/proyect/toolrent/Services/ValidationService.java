package com.proyect.toolrent.Services;


import com.proyect.toolrent.Entities.LoanXToolItemEntity;
import com.proyect.toolrent.Entities.ToolItemEntity;
import com.proyect.toolrent.Enums.KardexOperationType;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ValidationService {
    //Regex precompiled for efficiency
    private static final String NAME_REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String TOOL_NAME_REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ \\d*/\"'.#-,]+$";
    private static final String SERIAL_REGEX = "^[a-zA-Z0-9.-]{5,30}$";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String CLEAN_PHONE_REGEX = "[\\s\\-()]";
    private static final String CELLPHONE_REGEX = "\\+569\\d{8}";
    private final LoanXToolItemService loanXToolItemService;

    public ValidationService(LoanXToolItemService loanXToolItemService) {
        this.loanXToolItemService = loanXToolItemService;
    }

    //Verify that the user input doesn't contain SQL injections
    public boolean hasMaliciousQuery(String input){
        String lowerCase = input.toLowerCase();
        return lowerCase.contains("drop") || lowerCase.contains("delete") || lowerCase.contains("insert") || lowerCase.contains("update")
                || lowerCase.contains("select") || lowerCase.contains("truncate") || lowerCase.contains("--") ||  lowerCase.contains(";");
    }

    //Client and employee verification
    //Calculate the verifier digit
    private static char calculateDv(int run){
        int add = 0;
        int factor = 2;

        while(run > 0){
            int digit = run % 10;
            add += factor * digit;
            run /= 10;

            factor = (factor == 7) ? 2 : factor + 1;
        }

        int rest = 11 - (add % 11);
        if(rest == 11) return '0';
        if(rest == 10) return 'K';
        return (char) (rest + '0');
    }

    //Verify that the run is valid
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidRun(String run){
        //Null and Malicious Query validation
        if(run == null || run.isEmpty() || hasMaliciousQuery(run)) return false;

        //Normalize the run
        run = run.replace(".","").replace("-","").toUpperCase();

        if(run.length() < 2) return false;

        /*
        String body = run.substring(0, run.length() - 1);
        char dv = run.charAt(run.length() - 1);

        try{
            int runNum = Integer.parseInt(body);
            char calcualtedDv = calculateDv(runNum);
            return dv == calcualtedDv;

        } catch (NumberFormatException e){
            return false;
        }
        */
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidName(String name){
        //Name and Malicious Query validation
        return name != null && name.matches(NAME_REGEX) && !hasMaliciousQuery(name);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidEmail(String email){
        //Email and Malicious Query validation
        return email != null && email.matches(EMAIL_REGEX) && !hasMaliciousQuery(email);
    }

    //Reformat the cellphone string top match chilean format
    public String reformatCellphone(String cellphone){
        String clean = cellphone.replaceAll(CLEAN_PHONE_REGEX, "");

        if(clean.startsWith("+56")) return clean;
        else if(clean.startsWith("0")) return "+56" + clean.substring(1);
        else if(clean.length() == 9 && clean.startsWith("9")) return "+56"  + clean;

        return clean;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidCellphone(String cellphone){
        //Null and Malicious Query validation
        if(cellphone == null || cellphone.isEmpty() || hasMaliciousQuery(cellphone)) return false;

        String reformatedCellphone = reformatCellphone(cellphone);
        return reformatedCellphone.matches(CELLPHONE_REGEX) && reformatedCellphone.length() == 12;
    }

    public boolean isValidStatus(String status){
        //Null and Malicious Query validation
        if(status == null || status.isEmpty() || hasMaliciousQuery(status)) return false;

        status = status.toLowerCase();
        return status.equals("activo") || status.equals("restringido");
    }

    //Tool validation
    //Validates that a number is positive or 0 (stock, all fees and replacement value)
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidSerialNumber(String serialNumber){
        return serialNumber != null && serialNumber.matches(SERIAL_REGEX) && !hasMaliciousQuery(serialNumber);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidNumber(Integer number){
        return number != null && number >= 0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidToolName(String toolName){
        //Name of the tool and Malicious Query validation
        return toolName != null && toolName.matches(TOOL_NAME_REGEX) && !hasMaliciousQuery(toolName);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidToolStatus(String status){
        if(status == null || status.isEmpty() || hasMaliciousQuery(status)) return false;

        //Verify that the tool status is correct
        String formattedStatus = status.toUpperCase().replace(" ", "_");
        for(ToolStatus toolStatus : ToolStatus.values()){
            if(toolStatus.name().equals(formattedStatus)){
                return true;
            }
        }

        //If the for loop ends without finding an equality, it's invalid
        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidDamageLevel(String damageLevel){
        if(damageLevel == null || damageLevel.isEmpty() || hasMaliciousQuery(damageLevel)) return false;

        //Verify that the tool damage is correct
        String formattedDamageLevel = damageLevel.toUpperCase().replace(" ", "_");
        for(ToolDamageLevel toolDamageLevel : ToolDamageLevel.values()){
            if(toolDamageLevel.name().equals(formattedDamageLevel)){
                return true;
            }
        }

        //If the for loop ends without finding an equality, it's invalid
        return false;
    }

    //Kardex validation
    public boolean isValidOperationType(String operationType){
        if(operationType == null || operationType.isEmpty() || hasMaliciousQuery(operationType)) return false;

        //Verify that the kardex operation type is correct
        String formattedOperationType = operationType.toUpperCase().replace(" ", "_");
        for(KardexOperationType kardexOperationType : KardexOperationType.values()){
            if(kardexOperationType.name().equals(formattedOperationType)){
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidDate(LocalDate date){
        return date != null && !date.isBefore(LocalDate.now());
    }

    public boolean isValidReturnDate(LocalDate returnDate, LocalDate loanDate){ return returnDate != null && returnDate.isAfter(loanDate); }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isValidTime(LocalTime time){
        return time != null && !time.isBefore(LocalTime.now());
    }

    //Loan validation
    public void areToolsInStockOrAvailable(List<LoanXToolItemEntity> loanToolItems, StringBuilder toolsNotAvailable){
        for(LoanXToolItemEntity loanToolItem : loanToolItems){
            ToolItemEntity toolItem =  loanToolItem.getToolItem();
            if(loanXToolItemService.getAvailableStock(toolItem) == 0){
                //The tools names are saved so it can be displayed
                if(!toolsNotAvailable.isEmpty()) {
                    toolsNotAvailable.append(", ");
                    toolsNotAvailable.append(toolItem.getToolType().getName());
                } else{
                    toolsNotAvailable.append(toolItem.getToolType().getName());
                }
            }

            if(toolItem.getStatus() != ToolStatus.DISPONIBLE){
                if(!toolsNotAvailable.isEmpty()) {
                    toolsNotAvailable.append(", ");
                    toolsNotAvailable.append(toolItem.getToolType().getName());
                } else{
                    toolsNotAvailable.append(toolItem.getToolType().getName());
                }
            }
        }
    }

}
