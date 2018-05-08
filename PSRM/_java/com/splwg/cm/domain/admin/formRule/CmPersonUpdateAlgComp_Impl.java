package com.splwg.cm.domain.admin.formRule;

import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.customerinfo.address.Address;
import com.splwg.tax.domain.customerinfo.address.Address_Id;
import com.splwg.tax.domain.customerinfo.person.Person;
import com.splwg.tax.domain.customerinfo.person.Person_Id;

/**
 * @author CISSYS
 *
@AlgorithmComponent ()
 */
public class CmPersonUpdateAlgComp_Impl extends CmPersonUpdateAlgComp_Gen
        implements FormRuleBORuleProcessingAlgorithmSpot {

Logger logger = LoggerFactory.getLogger(CmPersonUpdateAlgComp_Impl.class);
    
    private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
    private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;
    
    @Override
    public void invoke() {
        
        logger.info("Executing Person Update Algorithm");  
        
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
            
            logger.info("Original Person BO: " + personBoInstance.getDocument().asXML());
            
            // Updating Person Information
            personBoInstance.setXMLFieldStringFromPath("personName[isPrimaryName = 'true' and nameType = 'PRIM']/firstName", (String) formBoInstance.getFieldAndMDForPath("personInformation/firstName/asCurrent").getValue());
            personBoInstance.setXMLFieldStringFromPath("personName[isPrimaryName = 'true' and nameType = 'PRIM']/lastName", (String) formBoInstance.getFieldAndMDForPath("personInformation/lastName/asCurrent").getValue());
            personBoInstance.setXMLFieldStringFromPath("personIds[isPrimaryId = 'true']/personIdNumber", (String) formBoInstance.getFieldAndMDForPath("identification/idValue/asCurrent").getValue());
            personBoInstance = BusinessObjectDispatcher.update(personBoInstance);
            
            logger.info("Updated Person BO: " + personBoInstance.getDocument().asXML());
            
            Address address = new Address_Id((String) personBoInstance.getFieldAndMDForPath("personAddress[sequence = '1']/addressId").getValue()).getEntity();
            
            // Creating Address BO Instance
            BusinessObjectInstance addressBoInstance = BusinessObjectInstance.create(address.getBusinessObject());
            
            // Set Address Id
            addressBoInstance.set("addressId", address.getId().getTrimmedValue());
            
            // Read Address 
            addressBoInstance = BusinessObjectDispatcher.read(addressBoInstance);
            
            logger.info("Original Address BO: " + addressBoInstance.getDocument().asXML());

            // Updating Address Information
            addressBoInstance.setXMLFieldStringFromPath("address1", (String) formBoInstance.getFieldAndMDForPath("address/address1/asCurrent").getValue());
            addressBoInstance.setXMLFieldStringFromPath("address2", (String) formBoInstance.getFieldAndMDForPath("address/address2/asCurrent").getValue());
            addressBoInstance.setXMLFieldStringFromPath("address3", (String) formBoInstance.getFieldAndMDForPath("address/address3/asCurrent").getValue());
            addressBoInstance.setXMLFieldStringFromPath("address4", (String) formBoInstance.getFieldAndMDForPath("address/address4/asCurrent").getValue());
            addressBoInstance.setXMLFieldStringFromPath("county", (String) formBoInstance.getFieldAndMDForPath("address/county/asCurrent").getValue());
            addressBoInstance.setXMLFieldStringFromPath("city", (String) formBoInstance.getFieldAndMDForPath("address/city/asCurrent").getValue());
            addressBoInstance.setXMLFieldStringFromPath("state", (String) formBoInstance.getFieldAndMDForPath("address/state/asCurrent").getValue());
            addressBoInstance.setXMLFieldStringFromPath("postal", (String) formBoInstance.getFieldAndMDForPath("address/postal/asCurrent").getValue());
            addressBoInstance.setXMLFieldStringFromPath("country", (String) formBoInstance.getFieldAndMDForPath("address/country/asCurrent").getValue());
            addressBoInstance = BusinessObjectDispatcher.update(addressBoInstance);
            
            logger.info("Updated Address BO: " + personBoInstance.getDocument().asXML());
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