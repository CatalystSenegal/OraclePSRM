package com.splwg.cm.domain.admin.formRule;

import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.FormRule;
import com.splwg.tax.domain.customerinfo.address.Address;
import com.splwg.tax.domain.customerinfo.address.Address_Id;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.person.Person_Id;
/**
 * @author CISSYS
 *
@AlgorithmComponent ()
 */
public class CmLoadPersonLoadAlgCompV2_Impl extends CmLoadPersonLoadAlgCompV2_Gen
        implements FormRuleBORuleProcessingAlgorithmSpot {
    
Logger logger = LoggerFactory.getLogger(CmLoadPersonLoadAlgComp_Impl.class);
    
    private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
    private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;
    @Override
    
    public void invoke() {
    logger.info("Executing Person Load Algorithm");  
    
    // Form Data BO Instance
    BusinessObjectInstance formBoInstance = (BusinessObjectInstance) applyFormRuleAlgorithmInputOutputData.getFormBusinessObject();
    logger.info("Input Form BO: " + formBoInstance.getDocument().asXML());

    // Check if Person Id is provided
    Person person = new Person_Id((String) formBoInstance.getFieldAndMDForPath("personInformation/personId/asCurrent").getValue()).getEntity();
    
    logger.info("Person: " + person);

    // Person BO
    BusinessObject_Id relatedTransactionBOId = person.getPersonType().getRelatedTransactionBOId();

    BusinessObjectInstance personBoInstance = null;
    
    // Transaction BO must exist in order to create the Person
    if(notNull(relatedTransactionBOId)) {
        
        // Creating Person BO Instance
        personBoInstance = BusinessObjectInstance.create(relatedTransactionBOId.getEntity());
        
        // Set Person Id
        personBoInstance.set("personId", person.getId().getTrimmedValue());
        
        // Read Person 
        personBoInstance = BusinessObjectDispatcher.read(personBoInstance);
        
        logger.info("Person BO: " + personBoInstance.getDocument().asXML());
        
        // Assign Information back to the Form 
        String firstName = (String) personBoInstance.getFieldAndMDForPath("personName[isPrimaryName = 'true' and nameType = 'PRIM']/firstName").getValue();
        String lastName = (String) personBoInstance.getFieldAndMDForPath("personName[isPrimaryName = 'true' and nameType = 'PRIM']/lastName").getValue();
        String idType = (String) personBoInstance.getFieldAndMDForPath("personIds[isPrimaryId = 'true']/idType").getValue();
        String idValue = (String) personBoInstance.getFieldAndMDForPath("personIds[isPrimaryId = 'true']/personIdNumber").getValue();

        logger.info("Form MD: " + formBoInstance.getSchemaMD());
        
        // As Current Data
        if(getXMLString(formBoInstance, "personInformation/firstName/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("personInformation/firstName/asCurrent", firstName);
        if(getXMLString(formBoInstance, "personInformation/lastName/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("personInformation/lastName/asCurrent", lastName);
        if(getXMLString(formBoInstance, "identification/idValue/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("identification/idValue/asCurrent", idValue);
        
        // As Reported
        formBoInstance.setXMLFieldStringFromPath("identification/idType/asReported", idType);
        formBoInstance.setXMLFieldStringFromPath("personInformation/firstName/asReported", firstName);
        formBoInstance.setXMLFieldStringFromPath("personInformation/lastName/asReported", lastName);
        formBoInstance.setXMLFieldStringFromPath("identification/idValue/asReported", idValue);
        
        Address address = new Address_Id((String) personBoInstance.getFieldAndMDForPath("personAddress[sequence = '1']/addressId").getValue()).getEntity();
        
        // Creating Address BO Instance
        BusinessObjectInstance addressBoInstance = BusinessObjectInstance.create(address.getBusinessObject());
        
        // Set Address Id
        addressBoInstance.set("addressId", address.getId().getTrimmedValue());
        
        // Read Address 
        addressBoInstance = BusinessObjectDispatcher.read(addressBoInstance);
        
        logger.info("Address BO: " + addressBoInstance.getDocument().asXML());
        
        String address1 = (String) addressBoInstance.getFieldAndMDForPath("address1").getValue();
        String address2 = (String) addressBoInstance.getFieldAndMDForPath("address2").getValue();
        String address3 = (String) addressBoInstance.getFieldAndMDForPath("address3").getValue();
        String address4 = (String) addressBoInstance.getFieldAndMDForPath("address4").getValue();
        String county = (String) addressBoInstance.getFieldAndMDForPath("county").getValue();
        String city = (String) addressBoInstance.getFieldAndMDForPath("city").getValue();
        String state = (String) addressBoInstance.getFieldAndMDForPath("state").getValue();
        String postal = (String) addressBoInstance.getFieldAndMDForPath("postal").getValue();
        String country = (String) addressBoInstance.getFieldAndMDForPath("country").getValue();
        
        // As Current Data
        if(getXMLString(formBoInstance, "address/addressId/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/addressId/asCurrent", address.getId().getTrimmedValue());
        if(getXMLString(formBoInstance, "address/address1/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/address1/asCurrent", address1);
        if(getXMLString(formBoInstance, "address/address2/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/address2/asCurrent", address2);
        if(getXMLString(formBoInstance, "address/address3/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/address3/asCurrent", address3);
        if(getXMLString(formBoInstance, "address/address4/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/address4/asCurrent", address4);
        if(getXMLString(formBoInstance, "address/county/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/county/asCurrent", county);
        if(getXMLString(formBoInstance, "address/city/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/city/asCurrent", city);
        if(getXMLString(formBoInstance, "address/state/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/state/asCurrent", state);
        if(getXMLString(formBoInstance, "address/postal/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/postal/asCurrent", postal);
        if(getXMLString(formBoInstance, "address/country/asCurrent").equals(""))
            formBoInstance.setXMLFieldStringFromPath("address/country/asCurrent", country);

        // As Reported
        formBoInstance.setXMLFieldStringFromPath("address/addressId/asReported", address.getId().getTrimmedValue());
        formBoInstance.setXMLFieldStringFromPath("address/address1/asReported", address1);
        formBoInstance.setXMLFieldStringFromPath("address/address2/asReported", address2);
        formBoInstance.setXMLFieldStringFromPath("address/address3/asReported", address3);
        formBoInstance.setXMLFieldStringFromPath("address/address4/asReported", address4);
        formBoInstance.setXMLFieldStringFromPath("address/county/asReported", county);
        formBoInstance.setXMLFieldStringFromPath("address/city/asReported", city);
        formBoInstance.setXMLFieldStringFromPath("address/state/asReported", state);
        formBoInstance.setXMLFieldStringFromPath("address/postal/asReported", postal);
        formBoInstance.setXMLFieldStringFromPath("address/country/asReported", country);


    }

    
}

private String getXMLString(BusinessObjectInstance formBoInstance, String xpath) {
    String firstNameAsCurrent = (String) formBoInstance.getFieldAndMDForPath(xpath).getValue();
    if(firstNameAsCurrent == null)
        return "";
    return firstNameAsCurrent;
}

@Override
public void setApplyFormRuleAlgorithmInputData(ApplyFormRuleAlgorithmInputData paramApplyFormRuleAlgorithmInputData) {
    applyFormRuleAlgorithmInputData = paramApplyFormRuleAlgorithmInputData;
}

@Override
public void setApplyFormRuleAlgorithmInputOutputData(ApplyFormRuleAlgorithmInputOutputData paramApplyFormRuleAlgorithmInputOutputData) {
    applyFormRuleAlgorithmInputOutputData = paramApplyFormRuleAlgorithmInputOutputData;
}

@Override
public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
    return applyFormRuleAlgorithmInputOutputData;
}
}
