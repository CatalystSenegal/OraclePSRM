package com.splwg.cm.domain.algo;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.Money;
import com.splwg.cm.domain.admin.formRule.CmPersonImmatricutionEmployeur_Impl;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRule;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;

/**
 * @author CISSYS
 *
@AlgorithmComponent ()
 */
public class EmployeDMT_Impl extends EmployeDMT_Gen implements FormRuleBORuleProcessingAlgorithmSpot {

    Logger logger = LoggerFactory.getLogger(CmPersonImmatricutionEmployeur_Impl.class);
   
    private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
    private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;
    @Override
    public void invoke() {
        // Form Data BO Instance
        BusinessObjectInstance formBoInstance = (BusinessObjectInstance) applyFormRuleAlgorithmInputOutputData
                .getFormBusinessObject();
        logger.info("Input Form BO: " + formBoInstance.getDocument().asXML());
        logger.info("formBoInstance: " + formBoInstance.getSchemaName());
        // Form Rule
//        FormRule formRule = applyFormRuleAlgorithmInputData.getFormRuleId().getEntity();
//
//        logger.info("Input FormRule: " + formRule);
//        logger.info("Input Name: " + formRule.entityName());
//        // Reading Form Rule Information
//        BusinessObjectInstance formRuleBoInstance = BusinessObjectInstance.create(formRule.getBusinessObject()); 
//        logger.info("formRuleBoInstance: " + formRuleBoInstance.getSchemaName());
//        logger.info("formRuleBoInstance: " + formRuleBoInstance.getDocument().asXML());
//        formRuleBoInstance.set("bo", formRule.getBusinessObject().getId().getTrimmedValue());
//        formRuleBoInstance.set("formRuleGroup", formRule.getId().getFormRuleGroup().getId().getTrimmedValue());
//        formRuleBoInstance.set("formRule", formRule.getId().getFormRule());
//        formRuleBoInstance.set("sequence", BigDecimal.valueOf(formRule.getSequence().longValue()));
//        formRuleBoInstance = BusinessObjectDispatcher.read(formRuleBoInstance);
        
     // ------------------------------Insertion des donnees dans la table CM_DMT_HISTORIQUE----------------------------------
        
        // -------------------------------Recuperation des donnees-------------------------------------------------------------------
        String idEmployeur = (String) formBoInstance.getFieldAndMDForPath("dmt/employeur/asCurrent").getValue();
        String typeMouvement = (String) formBoInstance.getFieldAndMDForPath("dmt/typeMouvement/asCurrent")
                .getValue();
        //Date dateEmbaucheDmt2 = (Date) formBoInstance.getFieldAndMDForPath("dmt/dateEmbauche/asCurrent")
               // .getValue();
        //BigDecimal ninEmployeeDmt2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("dmt/nin/asCurrent")
           //     .getValue();
        BigDecimal nin=new BigDecimal(12365);
        String nomEmployeeDmt= (String) formBoInstance.getFieldAndMDForPath("dmt/nom/asCurrent")
                .getValue();
        String prenomEmployeeDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/prenom/asCurrent")
                .getValue();
//        Date dateEntreeEtsDmt2 = (Date) formBoInstance.getFieldAndMDForPath("dmt/dateEntreeEts/asCurrent")
//                .getValue();
//        Date dateDeclarationEmbaucheDmt2 = (Date) formBoInstance.getFieldAndMDForPath("dmt/dateDeclarationEmb/asCurrent")
//                .getValue();
        String metierDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/metier/asCurrent")
                .getValue();
        String emploEtsDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/emploiEts/asCurrent")
                .getValue();
        String emploiReferenceDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/emploiReference/asCurrent")
                .getValue();
        String convCollectiveDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/conventionCollective/asCurrent")
                .getValue();
        String categorieDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/categorie/asCurrent")
                .getValue();
        String natureContratDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/natureContrat/asCurrent")
                .getValue();
//        Date dateDebutContratDmt2 = (Date) formBoInstance.getFieldAndMDForPath("dmt/dateDebutContrat/asCurrent")
//                .getValue();
//        Date dateFinContratDmt2 = (Date) formBoInstance.getFieldAndMDForPath("dmt/dateFinContrat/asCurrent")
//                .getValue();
//        BigDecimal salaireContractuelDmt2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("dmt/salaireContractuel/asCurrent")
      //          .getValue();
        BigDecimal salaire=new BigDecimal(10000);
      //  NUMBER salaireContractuelDmt=(NUMBER) salaireContractuelDmt2;
        //BigDecimal pourcentageEmploiDmt2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("dmt/pourcentageEmploi/asCurrent")
           //     .getValue();
        BigDecimal pourcentage=new BigDecimal(5);
        String employeATMPDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/employeATMP/asCurrent")
                .getValue();
       String lieuTravailDmt = (String) formBoInstance.getFieldAndMDForPath("dmt/lieuTravail/asCurrent")
                .getValue();
      BigDecimal nbreHeureTravail=new BigDecimal(20);
        String typeRelationEmp = (String) formBoInstance.getFieldAndMDForPath("dmt/typeRelationEmp/asCurrent")
                .getValue();
//        BigDecimal nbrHeureTravail = (BigDecimal) formBoInstance.getFieldAndMDForPath("dmt/nombreHeureTravail/asCurrent")
//                .getValue();
        
        BusinessObjectInstance histoBoInstance = BusinessObjectInstance 
                .create("CM_DmtHistoriqueBO");
        histoBoInstance.set("typeMouvement", typeMouvement);
        histoBoInstance.set("dateEmbauche", getSystemDateTime().getDate());
        histoBoInstance.set("idTravailleur", "3768125800"); 
        histoBoInstance.set("nin",nin);
        histoBoInstance.set("nom", nomEmployeeDmt);
        histoBoInstance.set("prenom", prenomEmployeeDmt);
        histoBoInstance.set("dateEntreeEts",getSystemDateTime().getDate());
        histoBoInstance.set("dateEntreeEmb", getSystemDateTime().getDate());
        histoBoInstance.set("metier", metierDmt);
        histoBoInstance.set("emploiEts", emploEtsDmt);
        histoBoInstance.set("emploiRef", emploiReferenceDmt);
        histoBoInstance.set("conventionCollective", convCollectiveDmt);
        histoBoInstance.set("categorie", categorieDmt);
        histoBoInstance.set("natureContrat", natureContratDmt);
        histoBoInstance.set("dateDebutContrat", getSystemDateTime().getDate());
        histoBoInstance.set("dateFinContrat", getSystemDateTime().getDate());
        histoBoInstance.set("salaireContractuel", salaire);
        histoBoInstance.set("pourcentageEmploi", pourcentage);
        histoBoInstance.set("employeATMP", employeATMPDmt);
        histoBoInstance.set("lieuTravail", lieuTravailDmt);
        histoBoInstance.set("idEmployeur", idEmployeur);
        histoBoInstance.set("typeRelationEmp", typeRelationEmp);
        histoBoInstance.set("nombreHeureTravail", nbreHeureTravail);
        histoBoInstance = BusinessObjectDispatcher.add(histoBoInstance);
        formBoInstance.setXMLFieldStringFromPath("dmt/travailleur/asCurrent",histoBoInstance.getString("idTravailleur"));
        formBoInstance.setXMLFieldStringFromPath("dmt/employeur/asCurrent",histoBoInstance.getString("idEmployeur")); 
        logger.info("histoBoInstance: " + histoBoInstance.getDocument().asXML());
    }

    @Override
    public void setApplyFormRuleAlgorithmInputData(
            ApplyFormRuleAlgorithmInputData paramApplyFormRuleAlgorithmInputData) {
        applyFormRuleAlgorithmInputData = paramApplyFormRuleAlgorithmInputData;
    }

    @Override
    public void setApplyFormRuleAlgorithmInputOutputData(
            ApplyFormRuleAlgorithmInputOutputData paramApplyFormRuleAlgorithmInputOutputData) {
        applyFormRuleAlgorithmInputOutputData = paramApplyFormRuleAlgorithmInputOutputData;
    }

    @Override
    public ApplyFormRuleAlgorithmInputOutputData getApplyFormRuleAlgorithmInputOutputData() {
        return applyFormRuleAlgorithmInputOutputData;
    }

}
