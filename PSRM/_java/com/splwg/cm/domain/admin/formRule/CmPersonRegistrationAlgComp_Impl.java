package com.splwg.cm.domain.admin.formRule;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.extendedLookupValue.ExtendedLookupValue;
import com.splwg.base.domain.security.accessGroup.AccessGroup;
import com.splwg.cm.domain.common.businessComponent.CmAccountRegistrationComponent;
import com.splwg.cm.domain.common.businessComponent.CmTaxRoleRegistrationComponent;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.DeliverableLookup;
import com.splwg.tax.api.lookup.NameTypeLookup;
import com.splwg.tax.domain.admin.accountRelationshipType.AccountRelationshipType;
import com.splwg.tax.domain.admin.customerClass.CustomerClass;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRule;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.admin.personType.PersonType;
import com.splwg.tax.domain.admin.serviceType.ServiceType;

/**
 * @author APerez
 *
 * @AlgorithmComponent ()
 */
public class CmPersonRegistrationAlgComp_Impl extends CmPersonRegistrationAlgComp_Gen
        implements FormRuleBORuleProcessingAlgorithmSpot {
    
    Logger logger = LoggerFactory.getLogger(CmPersonRegistrationAlgComp_Impl.class);
    
    private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
    private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;
    
    @Override
    public void invoke() {
        
        logger.info("Executing Person Registration Algorithm");
        
        // Form Data BO Instance
        BusinessObjectInstance formBoInstance = (BusinessObjectInstance) applyFormRuleAlgorithmInputOutputData.getFormBusinessObject();
        logger.info("Input Form BO: " + formBoInstance.getDocument().asXML());

        // Form Rule
        FormRule formRule = applyFormRuleAlgorithmInputData.getFormRuleId().getEntity();
        
        // Reading Form Rule Information
        BusinessObjectInstance formRuleBoInstance = BusinessObjectInstance.create(formRule.getBusinessObject());
        formRuleBoInstance.set("bo", formRule.getBusinessObject().getId().getTrimmedValue());
        formRuleBoInstance.set("formRuleGroup", formRule.getId().getFormRuleGroup().getId().getTrimmedValue());
        formRuleBoInstance.set("formRule", formRule.getId().getFormRule());
        formRuleBoInstance.set("sequence", BigDecimal.valueOf(formRule.getSequence().longValue()));
        formRuleBoInstance = BusinessObjectDispatcher.read(formRuleBoInstance);
        
        // Form Rule Details Group
        COTSInstanceNode ruleDetails = formRuleBoInstance.getGroup("ruleDetails");
        
        PersonType personType = ruleDetails.getEntity("personType", PersonType.class);
        
        // Check if Person Id is provided
        String firstName = (String) formBoInstance.getFieldAndMDForPath("personInformation/firstName/asCurrent").getValue();
        String lastName = (String) formBoInstance.getFieldAndMDForPath("personInformation/lastName/asCurrent").getValue();
        String personIdNumber = (String) formBoInstance.getFieldAndMDForPath("identification/idValue/asCurrent").getValue();
        IdType idType = new IdType_Id((String) formBoInstance.getFieldAndMDForPath("identification/idType/asCurrent").getValue()).getEntity();

        
        // Retrieve the Related Transaction BO from Person Type
        BusinessObject_Id relatedTransactionBOId = personType.getRelatedTransactionBOId();
        
        BusinessObjectInstance personBoInstance = null;
        
        // Transaction BO must exist in order to create the Person
        if(notNull(relatedTransactionBOId)) {
            
            // Creating Person BO Instance
            personBoInstance = BusinessObjectInstance.create(relatedTransactionBOId.getEntity());
            
            // Set Person Main Information
            personBoInstance.set("personType", personType);
            
            // Set Primary Person Name
            COTSInstanceListNode personNameInstance = personBoInstance.getList("personName").newChild();
            personNameInstance.set("nameType", NameTypeLookup.constants.PRIMARY);
            personNameInstance.set("isPrimaryName", Bool.TRUE);
            if(notNull(firstName) && notNull(lastName)) {
                personNameInstance.set("firstName", firstName);
                personNameInstance.set("lastName", lastName);
            } 
            
            // Set Person ID
            if(notNull(idType) && !isBlankOrNull(personIdNumber)) {
                COTSInstanceListNode personIdInstance = personBoInstance.getList("personIds").newChild();
                personIdInstance.set("isPrimaryId", Bool.TRUE);
                personIdInstance.set("idType", idType);
                personIdInstance.set("personIdNumber", personIdNumber);
            }
            
            // Invoke BO for Person Creation
            personBoInstance = BusinessObjectDispatcher.add(personBoInstance);
            
            // ...
            // Set Account Information
            CmAccountRegistrationComponent accountRegistrationHelper = CmAccountRegistrationComponent.Factory.newInstance();
            accountRegistrationHelper.setAccountType(ruleDetails.getEntity("accountType", CustomerClass.class));
            accountRegistrationHelper.setAccessGroup(ruleDetails.getEntity("accessGroup", AccessGroup.class));
            accountRegistrationHelper.setAccountRelationshipType(
                    ruleDetails.getEntity("accountRelationshipType", AccountRelationshipType.class));
            accountRegistrationHelper.setMainCustomerSwitch(ruleDetails.getBoolean("mainCustomerSwitch"));
            accountRegistrationHelper.setFinancialResponsibleSwitch(ruleDetails.getBoolean("financialResponsibleSwitch"));
            accountRegistrationHelper
                    .setCanReceiveNotificationSwitch(ruleDetails.getBoolean("canReceiveNotificationSwitch"));
            accountRegistrationHelper.setCanReceiveCopyOfBillSwitch(ruleDetails.getBoolean("canReceiveCopyOfBillSwitch"));
            accountRegistrationHelper.setBillAddressSource(ruleDetails.getLookup("billAddressSource"));
            accountRegistrationHelper
                    .setPersonIdString(personBoInstance.getString("personId")); 
//
//            // Creating Account
            BusinessObjectInstance accountInstance = accountRegistrationHelper.createAccount();
//
            logger.info("Creating Account BO: " +accountInstance.getDocument().asXML());
            
//            // Setting Tax Role information
            CmTaxRoleRegistrationComponent taxRoleRegistrationHelper = CmTaxRoleRegistrationComponent.Factory.newInstance();
            taxRoleRegistrationHelper.setServiceType(ruleDetails.getEntity("taxType", ServiceType.class));
            taxRoleRegistrationHelper
                    .setStartDate(getSystemDateTime().getDate());
            taxRoleRegistrationHelper
                    .setAccountIdString(accountInstance.getString("accountId"));  
            taxRoleRegistrationHelper.setFormType(applyFormRuleAlgorithmInputData.getFormTypeId().getEntity());
//
//            // Creating Tax Role
            BusinessObjectInstance taxRoleBoInstance = taxRoleRegistrationHelper.createTaxRoleBO(); 
            
            // Setting the account and the tax Role
            formBoInstance.setXMLFieldStringFromPath("accountAndTaxeRole/account/asCurrent",
                    accountInstance.getString("accountId"));
            
            formBoInstance.setXMLFieldStringFromPath("accountAndTaxeRole/taxRole/asCurrent",
                    taxRoleBoInstance.getString("taxRoleId"));
            
            logger.info("Creating Tax Role BO: " +taxRoleBoInstance.getDocument().asXML());
            
        }
        
        // Assign Account Id to the Form for further Form Rules processing
        formBoInstance.setXMLFieldStringFromPath("personInformation/personId/asCurrent",
                personBoInstance.getString("personId"));

        // ...
        
        // Assign Account Id to the Form for further Form Rules processing 
        formBoInstance.setXMLFieldStringFromPath("personInformation/personId/asCurrent", personBoInstance.getString("personId"));

        
     // ...

        ExtendedLookupValue addressType = ruleDetails.getExtendedLookupId("addressType").getEntity();

        // ...

        // Check if Address Id is provided
        String country_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/country/asCurrent")
                .getValue();
        String address1_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/address1/asCurrent")
                .getValue();
        String address2_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/address2/asCurrent")
                .getValue();
        String address3_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/address3/asCurrent")
                .getValue();
        String address4_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/address4/asCurrent")
                .getValue();
        String streetNumber1_value_form_path = (String) formBoInstance
                .getFieldAndMDForPath("address/streetNumber1/asCurrent").getValue();
        String streetNumber2_value_form_path = (String) formBoInstance
                .getFieldAndMDForPath("address/streetNumber1/asCurrent").getValue();
        String city_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/city/asCurrent").getValue();
        String county_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/county/asCurrent")
                .getValue();
        String state_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/state/asCurrent")
                .getValue();
        String postal_value_form_path = (String) formBoInstance.getFieldAndMDForPath("address/postal/asCurrent")
                .getValue();

        BusinessObjectInstance addresBoInstance = BusinessObjectInstance.create("C1-Address");
        
        logger.info("Creating addresBoInstance create: " +addresBoInstance.getDocument().asXML());
        
        addresBoInstance.set("bo", addresBoInstance.getBusinessObject());
        addresBoInstance.set("country", country_value_form_path);
        addresBoInstance.set("address1", address1_value_form_path);
        addresBoInstance.set("address2", address2_value_form_path);
        addresBoInstance.set("address3", address3_value_form_path);
        addresBoInstance.set("address4", address4_value_form_path);
        addresBoInstance.set("streetNumber1", streetNumber1_value_form_path);
        addresBoInstance.set("streetNumber2", streetNumber2_value_form_path);
        addresBoInstance.set("city", city_value_form_path);
        addresBoInstance.set("county", county_value_form_path);
        addresBoInstance.set("state", state_value_form_path);
        addresBoInstance.set("postal", postal_value_form_path);
        addresBoInstance = BusinessObjectDispatcher.add(addresBoInstance);

        logger.info("Creating addresBoInstance add: " +addresBoInstance.getDocument().asXML());
        
        // Setting Address Information
        COTSInstanceListNode personAddressInstance = personBoInstance.getList("personAddress").newChild();
        personAddressInstance.set("addressId", addresBoInstance.getString("addressId"));
        personAddressInstance.set("addressType", addressType.getId());
        personAddressInstance.set("startDate", getSystemDateTime().getDate());
        personAddressInstance.set("deliverable", DeliverableLookup.constants.YES);

        // ...

        // Assign Address Id to the Form for further Form Rules processing
        formBoInstance.setXMLFieldStringFromPath("address/addressId/asCurrent",
                addresBoInstance.getString("addressId"));
        
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
