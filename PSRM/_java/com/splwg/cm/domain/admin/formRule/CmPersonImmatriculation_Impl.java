package com.splwg.cm.domain.admin.formRule;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.NameTypeLookup;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRule;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.idType.IdType;
import com.splwg.tax.domain.admin.idType.IdType_Id;
import com.splwg.tax.domain.admin.personType.PersonType;

/**
 * @author CISSYS
 *
 * @AlgorithmComponent ()
 */
public class CmPersonImmatriculation_Impl extends CmPersonImmatriculation_Gen
        implements FormRuleBORuleProcessingAlgorithmSpot {
    Logger logger = LoggerFactory.getLogger(CmPersonRegistrationAlgComp_Impl.class);

    private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
    private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;

    @Override
    public void invoke() {
        // TODO Auto-generated method stub

        logger.info("Executing Person Registration Algorithm");

        // Form Data BO Instance
        BusinessObjectInstance formBoInstance = (BusinessObjectInstance) applyFormRuleAlgorithmInputOutputData
                .getFormBusinessObject();
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
        String firstName = (String) formBoInstance.getFieldAndMDForPath("information/firstName/asCurrent")
                .getValue();
        String lastName = (String) formBoInstance.getFieldAndMDForPath("information/lastName/asCurrent")
                .getValue();
        String personIdNumber = (String) formBoInstance.getFieldAndMDForPath("identification/idNumber/asCurrent")
                .getValue();
        IdType idType = new IdType_Id(
                (String) formBoInstance.getFieldAndMDForPath("identification/idType/asCurrent").getValue()).getEntity();

        // Retrieve the Related Transaction BO from Person Type
        BusinessObject_Id relatedTransactionBOId = personType.getRelatedTransactionBOId();

        BusinessObjectInstance personBoInstance = null;

        // Transaction BO must exist in order to create the Person
        if (notNull(relatedTransactionBOId)) {

            // Creating Person BO Instance
            personBoInstance = BusinessObjectInstance.create(relatedTransactionBOId.getEntity());

            // Set Person Main Information
            personBoInstance.set("personType", personType);

            // Set Primary Person Name
            COTSInstanceListNode personNameInstance = personBoInstance.getList("personName").newChild();
            personNameInstance.set("nameType", NameTypeLookup.constants.PRIMARY);
            personNameInstance.set("isPrimaryName", Bool.TRUE);
            if (notNull(firstName) && notNull(lastName)) {
                personNameInstance.set("firstName", firstName);
                personNameInstance.set("lastName", lastName);
            }

            // Set Person ID
            if (notNull(idType) && !isBlankOrNull(personIdNumber)) {
                COTSInstanceListNode personIdInstance = personBoInstance.getList("personIds").newChild();
                personIdInstance.set("isPrimaryId", Bool.TRUE);
                personIdInstance.set("idType", idType);
                personIdInstance.set("personIdNumber", personIdNumber);
            }

            // Invoke BO for Person Creation
            personBoInstance = BusinessObjectDispatcher.add(personBoInstance);
            formBoInstance.setXMLFieldStringFromPath("information/personId/asCurrent",
                    personBoInstance.getString("personId"));
        }
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


