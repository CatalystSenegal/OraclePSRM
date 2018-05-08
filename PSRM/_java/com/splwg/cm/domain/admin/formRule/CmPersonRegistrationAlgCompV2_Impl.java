package com.splwg.cm.domain.admin.formRule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.icu.math.BigDecimal;
import com.splwg.base.api.businessObject.BusinessObjectDispatcher;
import com.splwg.base.api.businessObject.BusinessObjectInstance;
import com.splwg.base.api.businessObject.COTSInstanceListNode;
import com.splwg.base.api.businessObject.COTSInstanceNode;
import com.splwg.base.api.datatypes.Bool;
import com.splwg.base.api.datatypes.Date;
import com.splwg.base.api.datatypes.EntityId;
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.extendedLookupValue.ExtendedLookupValue;
import com.splwg.base.domain.security.accessGroup.AccessGroup;
import com.splwg.cm.domain.common.businessComponent.CmAccountRegistrationComponent;
import com.splwg.cm.domain.common.businessComponent.CmTaxRoleRegistrationComponent;
//import com.splwg.cm.domain.hr.CmHistorique;
//import com.splwg.cm.domain.hr.CmHistorique_DTO;
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
 * @author CISSYS
 *
 * @AlgorithmComponent ()
 */
public class CmPersonRegistrationAlgCompV2_Impl extends CmPersonRegistrationAlgCompV2_Gen
        implements FormRuleBORuleProcessingAlgorithmSpot {

    Logger logger = LoggerFactory.getLogger(CmPersonRegistrationAlgComp_Impl.class);

    private ApplyFormRuleAlgorithmInputData applyFormRuleAlgorithmInputData;
    private ApplyFormRuleAlgorithmInputOutputData applyFormRuleAlgorithmInputOutputData;
    private List<String> listeCharType;
    private List<String> listeValueChar;
    private List<String> listeIDTypes = new ArrayList<String>();
    private List<String> listeIDValues = new ArrayList<String>();

    @Override
    public void invoke() {

        logger.info("Executing Person Registration Algorithm");

        // Form Data BO Instance
        BusinessObjectInstance formBoInstance = (BusinessObjectInstance) applyFormRuleAlgorithmInputOutputData // formBoInstance:
                                                                                                               // C'est
                                                                                                               // le
                                                                                                               // BO(CM-CM-IMMAT4)
                                                                                                               // genere
                                                                                                               // suite
                                                                                                               // a
                                                                                                               // la
                                                                                                               // creation
                                                                                                               // du
                                                                                                               // Form
                                                                                                               // Type.
                .getFormBusinessObject();
        logger.info("Input Form BO: " + formBoInstance.getDocument().asXML());
        logger.info("formBoInstance: " + formBoInstance.getSchemaName());
        // Form Rule
        FormRule formRule = applyFormRuleAlgorithmInputData.getFormRuleId().getEntity();

        logger.info("Input FormRule: " + formRule);
        logger.info("Input Name: " + formRule.entityName());
        // Reading Form Rule Information
        BusinessObjectInstance formRuleBoInstance = BusinessObjectInstance.create(formRule.getBusinessObject()); // formRuleBoInstance:
                                                                                                                 // c'est
                                                                                                                 // le
                                                                                                                 // formRuleBO
                                                                                                                 // CM-IMMAT-FRBO

        logger.info("formRuleBoInstance: " + formRuleBoInstance.getSchemaName());
        logger.info("formRuleBoInstance: " + formRuleBoInstance.getDocument().asXML());
        formRuleBoInstance.set("bo", formRule.getBusinessObject().getId().getTrimmedValue());
        formRuleBoInstance.set("formRuleGroup", formRule.getId().getFormRuleGroup().getId().getTrimmedValue());
        formRuleBoInstance.set("formRule", formRule.getId().getFormRule());
        formRuleBoInstance.set("sequence", BigDecimal.valueOf(formRule.getSequence().longValue()));
        formRuleBoInstance = BusinessObjectDispatcher.read(formRuleBoInstance);

        // Form Rule Details Group
        COTSInstanceNode ruleDetails = formRuleBoInstance.getGroup("ruleDetails");
        PersonType personType = ruleDetails.getEntity("personType", PersonType.class);

        // IdType idType = new IdType_Id(
        // (String)
        // formBoInstance.getFieldAndMDForPath("identification/idType/asCurrent").getValue()).getEntity();
        String ninea = (String) formBoInstance.getFieldAndMDForPath("identification/ninea/asCurrent").getValue();
        String numcss = (String) formBoInstance.getFieldAndMDForPath("identification/numcss/asCurrent").getValue();
        String numipres = (String) formBoInstance.getFieldAndMDForPath("identification/numipres/asCurrent").getValue();
        String numif = (String) formBoInstance.getFieldAndMDForPath("identification/identfiscale/asCurrent").getValue();
        String numrc = (String) formBoInstance.getFieldAndMDForPath("identification/registrecommerce/asCurrent")
                .getValue();
        String ninet = (String) formBoInstance.getFieldAndMDForPath("identification/ninet/asCurrent").getValue();

        listeIDTypes.add("NINEA");
        listeIDTypes.add("CSS");
        listeIDTypes.add("IPRES");
        listeIDTypes.add("UNIQ");
        listeIDTypes.add("AID");
        listeIDTypes.add("NIN");

        listeIDValues.add(ninea);
        listeIDValues.add(numcss);
        listeIDValues.add(numipres);
        listeIDValues.add(numif);
        listeIDValues.add(numrc);
        listeIDValues.add(ninet);
        // Retrieve the Related Transaction BO from Person Type
        BusinessObject_Id relatedTransactionBOId = personType.getRelatedTransactionBOId();

        BusinessObjectInstance personBoInstance = null;

        /*
         * #####################################################################
         * ## # CREATING PERSON BO INSTANCE 1 #
         * #####################################################################
         * ##
         */

        if (notNull(relatedTransactionBOId)) {

            // Creating Person BO Instance 1
            personBoInstance = BusinessObjectInstance.create(relatedTransactionBOId.getEntity());
            // personBoInstance =
            // BusinessObjectInstance.create("CM-PersonIndividual-DUP");
            logger.info("#####################################################: " + relatedTransactionBOId.getEntity()
                    + "#####################################################");

            // Set Person Main Information 1
            personBoInstance.set("personType", personType);

            // Set Person ID
            // if (notNull(idType) && !isBlankOrNull(ninea)) {
            if (listeIDValues != null) {
                for (int i = 0; i < listeIDValues.size(); i++) {
                    if (listeIDValues.get(i) != null) {
                        COTSInstanceListNode personIdInstance = personBoInstance.getList("personIds").newChild();
                        if (i == 0) {
                            personIdInstance.set("isPrimaryId", Bool.TRUE);
                            personIdInstance.set("idType", listeIDTypes.get(i));
                            personIdInstance.set("personIdNumber", listeIDValues.get(i));
                        } else {
                            personIdInstance.set("isPrimaryId", Bool.FALSE);
                            personIdInstance.set("idType", listeIDTypes.get(i));
                            personIdInstance.set("personIdNumber", listeIDValues.get(i));
                        }

                    }

                }
            } // FIN if (listeValues != null)
              // Dates
            Date datedepot_value_form_path = (Date) formBoInstance.getFieldAndMDForPath("Autres/datedepot/asCurrent")
                    .getValue();
            String datedepot2_value_form_path = datedepot_value_form_path.toString();
            Date dateinsptravail_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("Autres/dateinsptravail/asCurrent").getValue();
            String dateinsptravail2_value_form_path = dateinsptravail_value_form_path.toString();
            Date dateetablissement_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("Autres/dateetablissement/asCurrent").getValue();
            String dateetablissement2_value_form_path = dateetablissement_value_form_path.toString();
            Date dateembauche_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("Autres/dateembauche/asCurrent").getValue();
            String dateembauche2_value_form_path = dateembauche_value_form_path.toString();
            Date dateadhesion_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("Autres/dateadhesion/asCurrent").getValue();
            String dateadhesion2_value_form_path = dateadhesion_value_form_path.toString();
            Date dateffetadhesionregcadre_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("Autres/dateffetadhesionregcadre/asCurrent").getValue();
            String dateffetadhesionregcadre2_value_form_path = dateffetadhesionregcadre_value_form_path.toString();

            // Autres infos
            BigDecimal codenombrecss_value_form_path = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("Autres/codenombrecss/asCurrent").getValue();
            String codenombrecss2_value_form_path = codenombrecss_value_form_path.toString();
            BigDecimal codenombreemployesipres_value_form_path = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("Autres/codenombreemployesipres/asCurrent").getValue();
            String codenombreemployesipres2_value_form_path = codenombreemployesipres_value_form_path.toString();
            String website_value_form_path = (String) formBoInstance.getFieldAndMDForPath("Autres/website/asCurrent")
                    .getValue();
            String sceteursactivites_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("Autres/sceteursactivites/asCurrent").getValue();
            String activitesecondaires_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("Autres/activitesecondaires/asCurrent").getValue();
            // String origine_value_form_path = (String)
            // formBoInstance.getFieldAndMDForPath("Autres/origine/asCurrent")
            // .getValue();
            String agencecss_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("Autres/agencecss/asCurrent").getValue();
            BigDecimal travailleurscommun_value_form_path = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("Autres/travailleurscommun/asCurrent").getValue();
            String travailleurscommun2_value_form_path = travailleurscommun_value_form_path.toString();
            String agencesipres_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("Autres/agencesipres/asCurrent").getValue();
            BigDecimal travailleurscadres_value_form_path = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("Autres/travailleurscadres/asCurrent").getValue();
            String travailleurscadres2_value_form_path = travailleurscadres_value_form_path.toString();
            String telephone_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("Autres/telephone/asCurrent").getValue();
            String email_value_form_path = (String) formBoInstance.getFieldAndMDForPath("Autres/email/asCurrent")
                    .getValue();
            String raisonSociale_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("Autres/raisonsociale/asCurrent").getValue();
            // INFOS BANCAIRES
            Lookup usage_form_path_Look = (Lookup) formBoInstance.getFieldAndMDForPath("infosbancaires/usage/asCurrent")
                    .getValue();
            String usage_form_path = usage_form_path_Look.toString();
            String codeBanque_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("infosbancaires/codebanque/asCurrent").getValue();
            String codeguichet_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("infosbancaires/codeguichet/asCurrent").getValue();
            String compteBancaire_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("infosbancaires/comptebancaire/asCurrent").getValue();
            String rib_form_path = (String) formBoInstance.getFieldAndMDForPath("infosbancaires/RIB/asCurrent")
                    .getValue();
            String bic_form_path = (String) formBoInstance.getFieldAndMDForPath("infosbancaires/bic/asCurrent")
                    .getValue();
            String iban_form_path = (String) formBoInstance.getFieldAndMDForPath("infosbancaires/iban/asCurrent")
                    .getValue();

            listeCharType = new ArrayList<String>();
            listeValueChar = new ArrayList<String>();

            /*
             * #################################################################
             * ###### # CHARACTERISTICS TYPE #
             * #################################################################
             * ######
             */
            // DATES
            listeCharType.add("DDD");
            listeCharType.add("DIT");
            listeCharType.add("DE");
            listeCharType.add("DES");
            listeCharType.add("DEA");
            listeCharType.add("DEARC");

            // AUTRES
            listeCharType.add("NBCSS");
            listeCharType.add("NBIPRES");
            listeCharType.add("SITEWEB");
            listeCharType.add("SACTIVE");
            listeCharType.add("ACTSEC");
            // listeCharType.add("OEMP");
            listeCharType.add("ACSSR");
            listeCharType.add("NBTRDC");
            listeCharType.add("AIPRESR");
            listeCharType.add("NBTRC");

            // INFOS BANCAIRES
            listeCharType.add("CM_COMNT");
            listeCharType.add("CB");
            listeCharType.add("CG");
            listeCharType.add("NC");
            listeCharType.add("CR");
            listeCharType.add("BIC");
            listeCharType.add("IBAN");

            /*
             * #################################################################
             * ###### # CHARACTERISTICS VALUE #
             * #################################################################
             * ######
             */
            // Dates
            listeValueChar.add(datedepot2_value_form_path);
            listeValueChar.add(dateinsptravail2_value_form_path);
            listeValueChar.add(dateetablissement2_value_form_path);
            listeValueChar.add(dateembauche2_value_form_path);
            listeValueChar.add(dateadhesion2_value_form_path);
            listeValueChar.add(dateffetadhesionregcadre2_value_form_path);

            // Autres
            listeValueChar.add(codenombrecss2_value_form_path);
            listeValueChar.add(codenombreemployesipres2_value_form_path);
            listeValueChar.add(website_value_form_path);
            listeValueChar.add(sceteursactivites_value_form_path);
            listeValueChar.add(activitesecondaires_value_form_path);
            // listeValueChar.add(origine_value_form_path);
            listeValueChar.add(agencecss_value_form_path);
            listeValueChar.add(travailleurscommun2_value_form_path);
            listeValueChar.add(agencesipres_value_form_path);
            listeValueChar.add(travailleurscadres2_value_form_path);

            // INFOS BANCAIRES
            listeValueChar.add(usage_form_path);
            listeValueChar.add(codeBanque_form_path);
            listeValueChar.add(codeguichet_form_path);
            listeValueChar.add(compteBancaire_form_path);
            listeValueChar.add(rib_form_path);
            listeValueChar.add(bic_form_path);
            listeValueChar.add(iban_form_path);

            if (listeValueChar != null) {
                for (int i = 0; i < listeValueChar.size(); i++) {
                    if (listeValueChar.get(i) != "") {
                        COTSInstanceListNode personCharInstance = personBoInstance.getList("personChar").newChild();
                        personCharInstance.set("charTypeCD", listeCharType.get(i));
                        personCharInstance.set("charVal", "");
                        personCharInstance.set("adhocCharVal", listeValueChar.get(i));
                        personCharInstance.set("effectiveDate", getSystemDateTime().getDate());
                    }
                }
            }
            COTSInstanceListNode personNameInstance2 = personBoInstance.getList("personName").newChild();
            COTSInstanceListNode personPhoneInstance = personBoInstance.getList("personPhone").newChild();
            personNameInstance2.set("nameType", NameTypeLookup.constants.PRIMARY);
            personNameInstance2.set("isPrimaryName", Bool.TRUE);
            personNameInstance2.set("firstName", raisonSociale_value_form_path);
            personNameInstance2.set("lastName", raisonSociale_value_form_path);
            personPhoneInstance.set("phoneType", "HOME");
            personPhoneInstance.set("phone", telephone_value_form_path);
            personBoInstance.set("emailAddress", email_value_form_path);
            // ADDRESS
            ExtendedLookupValue addressType = ruleDetails.getExtendedLookupId("addressType").getEntity();

            // ...

            // Check if Address Id is provided

            String region_value_form_path = (String) formBoInstance.getFieldAndMDForPath("Autres/region/asCurrent")
                    .getValue();
            String departement_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("Autres/departement/asCurrent").getValue();
            String commune_value_form_path = (String) formBoInstance.getFieldAndMDForPath("Autres/commune/asCurrent")
                    .getValue();
            String ville_value_form_path = (String) formBoInstance.getFieldAndMDForPath("Autres/ville/asCurrent")
                    .getValue();
            String quartier_value_form_path = (String) formBoInstance.getFieldAndMDForPath("Autres/quartier/asCurrent")
                    .getValue();
            String adresse_value_form_path = (String) formBoInstance.getFieldAndMDForPath("Autres/adresse/asCurrent")
                    .getValue();
            String postal_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("Autres/boitepostale/asCurrent").getValue();
            BusinessObjectInstance addresBoInstance = BusinessObjectInstance.create("C1-Address");

            logger.info("Creating addresBoInstance create: " + addresBoInstance.getDocument().asXML());

            addresBoInstance.set("bo", addresBoInstance.getBusinessObject());
            addresBoInstance.set("address4", region_value_form_path);
            addresBoInstance.set("county", departement_value_form_path);
            addresBoInstance.set("address1", commune_value_form_path);
            addresBoInstance.set("city", ville_value_form_path);
            addresBoInstance.set("address2", quartier_value_form_path);
            addresBoInstance.set("address3", adresse_value_form_path);
            addresBoInstance.set("postal", postal_value_form_path);
            addresBoInstance = BusinessObjectDispatcher.add(addresBoInstance);

            logger.info("Creating addresBoInstance add: " + addresBoInstance.getDocument().asXML());

            // Setting Address Information
            COTSInstanceListNode personAddressInstance = personBoInstance.getList("personAddress").newChild();
            personAddressInstance.set("addressId", addresBoInstance.getString("addressId"));
            personAddressInstance.set("addressType", addressType.getId());
            personAddressInstance.set("startDate", getSystemDateTime().getDate());
            personAddressInstance.set("deliverable", DeliverableLookup.constants.YES);

            // ...
            // Assign Address Id to the Form for further Form Rules processing
            formBoInstance.setXMLFieldStringFromPath("Autres/addressId/asCurrent",
                    addresBoInstance.getString("addressId"));
            logger.info("Creating addressId1: " + addresBoInstance.getString("addressId"));

            // Invoke BO for Person Creation Enterprise
            personBoInstance = BusinessObjectDispatcher.add(personBoInstance);
            logger.info("Creating personBoInstance create: " + personBoInstance.getDocument().asXML());
            String personId1 = personBoInstance.getString("personId");
            logger.info("Creating personId1: " + personBoInstance.getString("personId"));
            // formBoInstance.setXMLFieldStringFromPath("representant/representantlegalid/asCurrent",
            // personBoInstance.getString("personId"));
            /*
             * #################################################################
             * ###### # FIN ENREGISTREMENT DES IFORMATIONS DE L'ENTREPRISE #
             * #################################################################
             * ######
             */

            /*
             * #################################################################
             * ###### # CREATING PERSON BO INSTANCE 2 REPRESENTANT LEGAL #
             * #################################################################
             * ######
             */
            personBoInstance = BusinessObjectInstance.create(relatedTransactionBOId.getEntity());
            // personType 2
            personBoInstance.set("personType", personType);
            // Check if Person Id is provided
            String firstName = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantprenoms/asCurrent").getValue();
            String lastName = (String) formBoInstance.getFieldAndMDForPath("representant/representantnoms/asCurrent")
                    .getValue();
            String email2_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantemail/asCurrent").getValue();
            String telephone2_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representanttelephone/asCurrent").getValue();
            // Set Primary Person Name
            COTSInstanceListNode personNameInstance = personBoInstance.getList("personName").newChild();
            COTSInstanceListNode personPhoneInstance2 = personBoInstance.getList("personPhone").newChild();
            personNameInstance.set("nameType", NameTypeLookup.constants.PRIMARY);
            personNameInstance.set("isPrimaryName", Bool.TRUE);
            if (notNull(firstName) && notNull(lastName)) {
                personNameInstance.set("firstName", firstName);
                personNameInstance.set("lastName", lastName);
                personPhoneInstance2.set("phoneType", "HOME");
                personPhoneInstance2.set("phone", telephone2_value_form_path);
                personBoInstance.set("emailAddress", email2_value_form_path);

            }
            // ADDRESS
            ExtendedLookupValue addressType2 = ruleDetails.getExtendedLookupId("addressType").getEntity();
            // ...
            // Check if Address Id is provided

            String region_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantregion/asCurrent").getValue();
            String departement_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantdepartement/asCurrent").getValue();
            String commune_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantcommune/asCurrent").getValue();
            String ville_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantville/asCurrent").getValue();
            String quartier_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantquartier/asCurrent").getValue();
            String adresse_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantadresse/asCurrent").getValue();
            String postal_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("representant/representantboitepostale/asCurrent").getValue();
            BusinessObjectInstance addresBoInstance2 = BusinessObjectInstance.create("C1-Address");

            logger.info("Creating addresBoInstance create: " + addresBoInstance2.getDocument().asXML());

            addresBoInstance2.set("bo", addresBoInstance2.getBusinessObject());
            addresBoInstance2.set("address4", region_value_form_path2);
            addresBoInstance2.set("county", departement_value_form_path2);
            addresBoInstance2.set("address1", commune_value_form_path2);
            addresBoInstance2.set("city", ville_value_form_path2);
            addresBoInstance2.set("address2", quartier_value_form_path2);
            addresBoInstance2.set("address3", adresse_value_form_path2);
            addresBoInstance2.set("postal", postal_value_form_path2);
            addresBoInstance2 = BusinessObjectDispatcher.add(addresBoInstance2);

            logger.info("Creating addressId2: " + addresBoInstance2.getString("addressId"));
            logger.info("Creating addresBoInstance add: " + addresBoInstance2.getDocument().asXML());

            // Setting Address Information
            COTSInstanceListNode personAddressInstance2 = personBoInstance.getList("personAddress").newChild();
            personAddressInstance2.set("addressId", addresBoInstance2.getString("addressId"));
            personAddressInstance2.set("addressType", addressType2.getId());
            personAddressInstance2.set("startDate", getSystemDateTime().getDate());
            personAddressInstance2.set("deliverable", DeliverableLookup.constants.YES);

            // FAIRE LA RELATION ENTRE LES DEUX PERSONNES CREEES

            // RELATION ENTRE PERSONNE 1 ET PERSONNE 2
            COTSInstanceListNode personPersonInstance1 = personBoInstance.getList("personPerson").newChild();
            personPersonInstance1.set("personId2", personId1);
            personPersonInstance1.set("personId1", personId1);
            personPersonInstance1.set("personRelationshipType", "SIBLING");
            personPersonInstance1.set("startDate", getSystemDateTime().getDate());

            // RELATION ENTRE PERSONNE 2 ET PERSONNE 1
            COTSInstanceListNode personPersonInstance2 = personBoInstance.getList("personPerson").newChild();
            personPersonInstance2.set("personId1", personId1);
            personPersonInstance2.set("personId2", personId1);
            personPersonInstance2.set("personRelationshipType", "EMPLOYEE");
            personPersonInstance2.set("startDate", getSystemDateTime().getDate());
            // Invoke BO for Person Creation Legal Entity
            personBoInstance = BusinessObjectDispatcher.add(personBoInstance);

            // }
            logger.info("Creating personId2: " + personBoInstance.getString("personId"));
            formBoInstance.setXMLFieldStringFromPath("representant/representantlegalid/asCurrent",
                    personBoInstance.getString("personId"));
            /*
             * #################################################################
             * ###### # FIN ENREGISTREMENT DES INFORMATIONS DU REPRESENTANT LEGAL
             * #
             * #################################################################
             * ######
             */

            /*
             * #################################################################
             * ###### # ENREGISTREMENT DES INFORMATION DES EMPLOYES #
             * #################################################################
             * ######
             */
            COTSInstanceNode group = formBoInstance.getGroupFromPath("employee");
            if (ruleDetails.getBoolean("isRecurrent").isTrue()) {
      Iterator<COTSInstanceListNode> iterator = group.getList("employeeList").iterator(); 
                logger.info("hasNext: " + iterator.hasNext());
               while (iterator.hasNext()) {
                   personBoInstance = BusinessObjectInstance.create(relatedTransactionBOId.getEntity());
                   personBoInstance.set("personType", personType);
                   COTSInstanceListNode nextElt = iterator.next();
                   if(nextElt!=null){
                    // Check if Person Id is provided
                    String firstNameEmployee = (String) nextElt.getFieldAndMDForPath("representantprenoms/asCurrent").getValue();
                    logger.info("firstNameEmployee: " + firstNameEmployee);
                    String lastNameEmployee = (String) nextElt.getFieldAndMDForPath("representantnoms/asCurrent").getValue();
                    logger.info("lastNameEmployee: " + lastNameEmployee);
                    String emailEmployee = (String) nextElt.getFieldAndMDForPath("representantemail/asCurrent").getValue();
                    logger.info("emailEmployee: " + emailEmployee);
                    String telephoneEmployee = (String) nextElt.getFieldAndMDForPath("representanttelephone/asCurrent").getValue();
                    logger.info("telephoneEmployee: " + telephoneEmployee);
                    // Set Primary Person Name
                    COTSInstanceListNode personNameInstanceEmployee = personBoInstance.getList("personName").newChild();
                    COTSInstanceListNode personPhoneInstance3 = personBoInstance.getList("personPhone").newChild();
                    personNameInstanceEmployee.set("nameType", NameTypeLookup.constants.PRIMARY);
                    personNameInstanceEmployee.set("isPrimaryName", Bool.TRUE);
                    if (notNull(firstNameEmployee) && notNull(lastNameEmployee)) {
                        personNameInstanceEmployee.set("firstName", firstNameEmployee); 
                        personNameInstanceEmployee.set("lastName", lastNameEmployee);
                        personPhoneInstance3.set("phoneType", "HOME");
                        personPhoneInstance3.set("phone", telephoneEmployee);
                        personBoInstance.set("emailAddress", emailEmployee);

                    }
                    // ADDRESS
                    ExtendedLookupValue addressType3 = ruleDetails.getExtendedLookupId("addressType").getEntity();
                    // ...
                    // Check if Address Id is provided

                    String regionEmplyee = (String) nextElt.getFieldAndMDForPath("representantregion/asCurrent").getValue();
                    String departementEmplyee = (String) nextElt.getFieldAndMDForPath("representantdepartement/asCurrent").getValue();
                    String communeEmplyee = (String) nextElt .getFieldAndMDForPath("representantcommune/asCurrent").getValue();
                    String villeEmplyee = (String) nextElt.getFieldAndMDForPath("representantville/asCurrent").getValue();
                    String quartierEmplyee = (String) nextElt.getFieldAndMDForPath("representantquartier/asCurrent").getValue();
                    String adresseEmplyee = (String) nextElt.getFieldAndMDForPath("representantadresse/asCurrent").getValue();
                    String postalEmplyee = (String) nextElt.getFieldAndMDForPath("representantboitepostale/asCurrent").getValue();
                    BusinessObjectInstance addresBoInstance3 = BusinessObjectInstance.create("C1-Address");

                    logger.info("Creating addresBoInstance create: " + addresBoInstance3.getDocument().asXML());

                    addresBoInstance3.set("bo", addresBoInstance3.getBusinessObject());
                    addresBoInstance3.set("address4", regionEmplyee);
                    addresBoInstance3.set("county", departementEmplyee);
                    addresBoInstance3.set("address1", communeEmplyee);
                    addresBoInstance3.set("city", villeEmplyee);
                    addresBoInstance3.set("address2", quartierEmplyee);
                    addresBoInstance3.set("address3", adresseEmplyee);
                    addresBoInstance3.set("postal", postalEmplyee);
                    addresBoInstance3 = BusinessObjectDispatcher.add(addresBoInstance3);

                    logger.info("Creating addressId3: " + addresBoInstance3.getString("addressId"));
                    logger.info("Creating addresBoInstance add: " + addresBoInstance3.getDocument().asXML());

                    // Setting Address Information
                    COTSInstanceListNode personAddressInstance3 = personBoInstance.getList("personAddress").newChild();
                    personAddressInstance3.set("addressId", addresBoInstance3.getString("addressId"));
                    personAddressInstance3.set("addressType", addressType3.getId());
                    personAddressInstance3.set("startDate", getSystemDateTime().getDate());
                    personAddressInstance3.set("deliverable", DeliverableLookup.constants.YES);

                    // FAIRE LA RELATION ENTRE LES DEUX PERSONNES CREEES

                    // RELATION ENTRE PERSONNE 1 ET PERSONNE 3
//                    COTSInstanceListNode personPersonInstance3 = personBoInstance.getList("personPerson").newChild();
//                    personPersonInstance3.set("personId2", personId1);
//                    personPersonInstance3.set("personId1", personId1);
//                    personPersonInstance3.set("personRelationshipType", "SIBLING");
//                    personPersonInstance3.set("startDate", getSystemDateTime().getDate());
                    logger.info("dateSysteme: " + getSystemDateTime().getDate()); 
                    // RELATION ENTRE PERSONNE 3 ET PERSONNE 1
                   // COTSInstanceListNode personPersonInstance4 = personBoInstance.getList("personPerson").newChild();
//                    personPersonInstance4.set("personId1", personId1);
//                    personPersonInstance4.set("personId2", personId1);
//                    personPersonInstance4.set("personRelationshipType", "SPOUSE");
//                    personPersonInstance4.set("startDate", getSystemDateTime().getDate());
//                    // Invoke BO for Person Creation Legal Entity
                    personBoInstance = BusinessObjectDispatcher.add(personBoInstance);
                    BusinessObjectInstance histoBoInstance = BusinessObjectInstance.create("CM_HistoriqueEmployeBO"); 
                     histoBoInstance.set("personId1",personId1);
                     histoBoInstance.set("personId2",personBoInstance.getString("personId")); 
                     histoBoInstance.set("startDate",getSystemDateTime().getDate()); 
                     histoBoInstance.set("endDate",getSystemDateTime().getDate());     
                     histoBoInstance.set("cmHistSalaire", "2000000"); 
                     histoBoInstance.set("cmHistTypeRelation", "Employe"); 
                     histoBoInstance=BusinessObjectDispatcher.add(histoBoInstance);  
                     logger.info("histoBoInstance: " + histoBoInstance.getDocument().asXML()); 
                    // }
                    logger.info("Creating personId3: " + personBoInstance.getString("personId"));
                    nextElt.setXMLFieldStringFromPath("representantlegalid/asCurrent",
                            personBoInstance.getString("personId"));

                    /*
                     * #########################################################
                     * ######## ###### # FIN ENREGISTREMENT DES INFORMATIS DES EMPLOYES #
                     * #########################################################
                     * ######## ######
                     */
                   } // FIN if
                } // FIN while(iterator.hasNext())
            } // FIN if(ruleDetails.getBoolean())
        } // FIN if (notNull(relatedTransactionBOId))

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
