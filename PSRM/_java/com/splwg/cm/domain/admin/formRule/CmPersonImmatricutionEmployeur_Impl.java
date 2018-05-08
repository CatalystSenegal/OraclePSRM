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
import com.splwg.base.api.datatypes.Lookup;
import com.splwg.base.api.datatypes.Money;
import com.splwg.base.domain.common.businessObject.BusinessObject_Id;
import com.splwg.base.domain.common.extendedLookupValue.ExtendedLookupValue;
import com.splwg.shared.logging.Logger;
import com.splwg.shared.logging.LoggerFactory;
import com.splwg.tax.api.lookup.DeliverableLookup;
import com.splwg.tax.api.lookup.NameTypeLookup;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputData;
import com.splwg.tax.domain.admin.formRule.ApplyFormRuleAlgorithmInputOutputData;
import com.splwg.tax.domain.admin.formRule.FormRule;
import com.splwg.tax.domain.admin.formRule.FormRuleBORuleProcessingAlgorithmSpot;
import com.splwg.tax.domain.admin.personType.PersonType;

import oracle.sql.NUMBER;

/**
 * @author CISSYS
 *
 * @AlgorithmComponent ()
 */
public class CmPersonImmatricutionEmployeur_Impl extends CmPersonImmatricutionEmployeur_Gen
        implements FormRuleBORuleProcessingAlgorithmSpot {

    Logger logger = LoggerFactory.getLogger(CmPersonImmatricutionEmployeur_Impl.class);

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
        BigDecimal ninea2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("employerQuery/nineaNumber/asCurrent").getValue();
        String ninea=ninea2.toString();
        BigDecimal numif2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("employerQuery/taxId/asCurrent").getValue();
        String numif=numif2.toString();
        BigDecimal numrc2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("employerQuery/tradeRegisterNumber/asCurrent")
                .getValue();
        String numrc=numrc2.toString();

        listeIDTypes.add("NINEA");
        listeIDTypes.add("NIF");
        listeIDTypes.add("NRC");

        listeIDValues.add(ninea);
        listeIDValues.add(numif);
        listeIDValues.add(numrc);
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
            personBoInstance = BusinessObjectInstance.create("CM-PersonIndividualDUP");
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
            Date datedesoumission_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/dateOfSubmission/asCurrent").getValue();
            String datedesoumission2_value_form_path = datedesoumission_value_form_path.toString();
            Date dateinsptravail_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/dateOfLabourInspection/asCurrent").getValue();
            String dateinsptravail2_value_form_path = dateinsptravail_value_form_path.toString();
            Date dateembauche_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/dateOfFirstHire/asCurrent").getValue();
            String dateembauche2_value_form_path = dateembauche_value_form_path.toString();
            Date dateadhesion_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/dateOfEffectiveMembership/asCurrent").getValue();
            String dateadhesion2_value_form_path = dateadhesion_value_form_path.toString();
            Date dateDeCreation_value_form_path = (Date) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/dateOfEstablishment/asCurrent").getValue();
            String dateDeCreation2_value_form_path = dateDeCreation_value_form_path.toString();

            // Autres infos
            String website_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/website/asCurrent").getValue();
            String secteursactivitePrincipale_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/mainLineOfBusiness/asCurrent").getValue();
            String secteursactivites_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/businessSector/asCurrent").getValue();
            BigDecimal tauxAT2_value_form_path = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/rate/asCurrent").getValue();
            String tauxAT_value_form_path = tauxAT2_value_form_path.toString();
            String secteursactivitesecondaires_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/secondaryLineOfBusiness/asCurrent").getValue();
            String convBranche_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/branchAgreement/asCurrent").getValue();
            String paymentMethod_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/paymentMethod/asCurrent").getValue();
            String dnsDeclaration_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/dnsDeclaration/asCurrent").getValue();
            String  travailleursRegimeGeneral_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/noOfWorkersInGenScheme/asCurrent").getValue();
            //String travailleursRegimeGeneral2_value_form_path = travailleursRegimeGeneral_value_form_path.toString();
            String travailleursRegimeDeBase_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/noOfWorkersInBasicScheme/asCurrent").getValue();
            //String travailleursRegimeDeBase2_value_form_path = travailleursRegimeDeBase_value_form_path.toString();
            String sigle_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/shortName/asCurrent").getValue();
            String telephone_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/telephone/asCurrent").getValue();
            String email_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/email/asCurrent").getValue();
            String raisonSociale_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/employerName/asCurrent").getValue();

            // INFOS BANCAIRES
            String usage_form_path_Look = (String) formBoInstance
                    .getFieldAndMDForPath("bankInformationForm/usage/asCurrent").getValue();
            String usage_form_path = usage_form_path_Look.toString();
            String codeBanque_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("bankInformationForm/bankCode/asCurrent").getValue();
            String codeguichet_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("bankInformationForm/codeBox/asCurrent").getValue();
            BigDecimal compteBancaire_form_path2 = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("bankInformationForm/accountNumber/asCurrent").getValue();
            String compteBancaire_form_path=compteBancaire_form_path2.toString();
            String rib_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("bankInformationForm/ribNumber/asCurrent").getValue();
            String bic_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("bankInformationForm/bicNumber/asCurrent").getValue();
            String iban_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("bankInformationForm/swiftCode/asCurrent").getValue();

            listeCharType = new ArrayList<String>();
            listeValueChar = new ArrayList<String>();

            /*
             * #################################################################
             * ###### # CHARACTERISTICS TYPE #
             * #################################################################
             * ######
             */
            // DATES
            listeCharType.add("DDS");
            listeCharType.add("DIT");
            listeCharType.add("DES");
            listeCharType.add("DAE");
            listeCharType.add("DDC");

            // AUTRES
            listeCharType.add("SACTIVE");
            listeCharType.add("TAT");
            listeCharType.add("SACTIVPR");
            listeCharType.add("SACTIVSE");
            listeCharType.add("CDB");
            listeCharType.add("MDPAF");
            listeCharType.add("DDNS");
            listeCharType.add("NBTRG");
            listeCharType.add("NBTRB");
            listeCharType.add("SIGLE");
            listeCharType.add("SINT");

            // INFOS BANCAIRES
            listeCharType.add("USAGE");
            listeCharType.add("CDBANK");
            listeCharType.add("CDGUCH");
            listeCharType.add("NUMCMPT");
            listeCharType.add("CLERIB");
            listeCharType.add("BIC");
            listeCharType.add("SWIFT");

            /*
             * #################################################################
             * ###### # CHARACTERISTICS VALUE #
             * #################################################################
             * ######
             */
            // Dates
            listeValueChar.add(datedesoumission2_value_form_path);
            listeValueChar.add(dateinsptravail2_value_form_path);
            listeValueChar.add(dateembauche2_value_form_path);
            listeValueChar.add(dateadhesion2_value_form_path);
            listeValueChar.add(dateDeCreation2_value_form_path);

            // Autres
            listeValueChar.add(secteursactivites_value_form_path);
            listeValueChar.add(tauxAT_value_form_path);
            listeValueChar.add(secteursactivitePrincipale_value_form_path);
            listeValueChar.add(secteursactivitesecondaires_value_form_path);
            listeValueChar.add(convBranche_value_form_path);
            listeValueChar.add(paymentMethod_value_form_path);
            listeValueChar.add(dnsDeclaration_value_form_path);
            listeValueChar.add(travailleursRegimeGeneral_value_form_path);
            listeValueChar.add(travailleursRegimeDeBase_value_form_path);
            listeValueChar.add(sigle_value_form_path);
            listeValueChar.add(website_value_form_path);

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
                        personCharInstance.set("searchCharVal", listeValueChar.get(i));
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
            personPhoneInstance.set("phone","(999) 999-9999");
            personBoInstance.set("emailAddress", email_value_form_path);
            // ADDRESS
            ExtendedLookupValue addressType = ruleDetails.getExtendedLookupId("addressType").getEntity();

            // ...

            // Check if Address Id is provided

            String region_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/region/asCurrent").getValue();
            String departement_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/department/asCurrent").getValue();
            // String commune_value_form_path = (String)
            // formBoInstance.getFieldAndMDForPath("mainRegistrationForm/commune/asCurrent")
            // .getValue();
            String ville_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/city/asCurrent").getValue();
            String quartier_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/district/asCurrent").getValue();
            String adresse_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/address/asCurrent").getValue();
            String postal_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/postbox/asCurrent").getValue();

            String zoneGeographique_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/geographicalArea/asCurrent").getValue();
            String zone_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/zone/asCurrent").getValue();
            String cssAgency_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/cssAgency/asCurrent").getValue();
            String ipresAgency_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("mainRegistrationForm/ipresAgency/asCurrent").getValue();

            BusinessObjectInstance addresBoInstance = BusinessObjectInstance.create("C1-Address");

            logger.info("Creating addresBoInstance create: " + addresBoInstance.getDocument().asXML());

            addresBoInstance.set("bo", addresBoInstance.getBusinessObject());
            addresBoInstance.set("address4", region_value_form_path);
            addresBoInstance.set("county", departement_value_form_path);
            addresBoInstance.set("address1", "Bambey");
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
//            formBoInstance.setXMLFieldStringFromPath("mainRegistrationForm/addressId/asCurrent",
//                    addresBoInstance.getString("addressId"));
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
            personBoInstance = BusinessObjectInstance.create("CM-PersonIndividualDUP");
            // personType 2
            personBoInstance.set("personType", personType);
            // Check if Person Id is provided
            BigDecimal nin2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("legalRepresentativeForm/nin/asCurrent")
                    .getValue();
            String nin=nin2.toString();
            String prenom = (String) formBoInstance.getFieldAndMDForPath("legalRepresentativeForm/lastName/asCurrent")
                    .getValue();
            String nom = (String) formBoInstance.getFieldAndMDForPath("legalRepresentativeForm/firstName/asCurrent")
                    .getValue();
//            Date dateDeNaissance = (Date) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/birthDate/asCurrent").getValue();
//            String lieuDeNaissance = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/placeOfBirth/asCurrent").getValue();
//            String nationalite = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/nationality/asCurrent").getValue();
//            String typeOfNationality = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/typeOfNationality/asCurrent").getValue();
//            String typeOfIdentity = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/typeOfIdentity/asCurrent").getValue();
//            String identityIdNumber = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/identityIdNumber/asCurrent").getValue();
//            String dateDelivrance = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/issueDate/asCurrent").getValue();
//            String telephoneFixe = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/landLineNumber/asCurrent").getValue();
//            String dateExpiration = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/expiryDate/asCurrent").getValue();
//            String photo = (String) formBoInstance.getFieldAndMDForPath("legalRepresentativeForm/photo/asCurrent")
//                    .getValue();
            String email2_value_form_path = (String) formBoInstance
                    .getFieldAndMDForPath("legalRepresentativeForm/email/asCurrent").getValue();
//            String telephoneMobile_value_form_path = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/mobileNumber/asCurrent").getValue();
            // Set Primary Person Id
            COTSInstanceListNode personIdInstance = personBoInstance.getList("personIds").newChild();
            personIdInstance.set("isPrimaryId", Bool.TRUE);
            personIdInstance.set("idType", "NIN");
            personIdInstance.set("personIdNumber", nin);
            // Set Primary Person Name
            COTSInstanceListNode personNameInstance = personBoInstance.getList("personName").newChild();
            COTSInstanceListNode personPhoneInstance2 = personBoInstance.getList("personPhone").newChild();
            personNameInstance.set("nameType", NameTypeLookup.constants.PRIMARY);
            personNameInstance.set("isPrimaryName", Bool.TRUE);
            if (notNull(prenom) && notNull(nom)) {
                personNameInstance.set("firstName", prenom);
                personNameInstance.set("lastName", nom);
                personPhoneInstance2.set("phoneType", "HOME");
                personPhoneInstance2.set("phone", "(999) 999-9999");
                personBoInstance.set("emailAddress", email2_value_form_path);

            }
            // ADDRESS
            ExtendedLookupValue addressType2 = ruleDetails.getExtendedLookupId("addressType").getEntity();
            // ...
            // Check if Address Id is provided

            String region_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("legalRepresentativeForm/region/asCurrent").getValue();
            String departement_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("legalRepresentativeForm/department/asCurrent").getValue();
//            String commune_value_form_path2 = (String) formBoInstance
//                    .getFieldAndMDForPath("legalRepresentativeForm/representantcommune/asCurrent").getValue();
            String ville_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("legalRepresentativeForm/city/asCurrent").getValue();
            String quartier_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("legalRepresentativeForm/district/asCurrent").getValue();
            String adresse_value_form_path2 = (String) formBoInstance
                    .getFieldAndMDForPath("legalRepresentativeForm/address/asCurrent").getValue();
            String boitePostale = (String) formBoInstance
                    .getFieldAndMDForPath("legalRepresentativeForm/postboxNumber/asCurrent").getValue();
            BusinessObjectInstance addresBoInstance2 = BusinessObjectInstance.create("C1-Address");

            logger.info("Creating addresBoInstance create: " + addresBoInstance2.getDocument().asXML());

            addresBoInstance2.set("bo", addresBoInstance2.getBusinessObject());
            addresBoInstance2.set("address4", region_value_form_path2);
            addresBoInstance2.set("county", departement_value_form_path2);
            addresBoInstance2.set("address1", "Bambey");
            addresBoInstance2.set("city", ville_value_form_path2);
            addresBoInstance2.set("address2", quartier_value_form_path2);
            addresBoInstance2.set("address3", adresse_value_form_path2);
            addresBoInstance2.set("postal", boitePostale);
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
            personPersonInstance1.set("personRelationshipType", "EMPL-EMP");
            personPersonInstance1.set("startDate", getSystemDateTime().getDate());

            // RELATION ENTRE PERSONNE 2 ET PERSONNE 1
            COTSInstanceListNode personPersonInstance2 = personBoInstance.getList("personPerson").newChild();
            personPersonInstance2.set("personId1", personId1);
            personPersonInstance2.set("personId2", personId1);
            personPersonInstance2.set("personRelationshipType", "SPOUSE  ");
            personPersonInstance2.set("startDate", getSystemDateTime().getDate());
            // Invoke BO for Person Creation Legal Entity
            personBoInstance = BusinessObjectDispatcher.add(personBoInstance);

            // }
            logger.info("Creating personId2: " + personBoInstance.getString("personId"));
//            formBoInstance.setXMLFieldStringFromPath("representant/representantlegalid/asCurrent",
//                    personBoInstance.getString("personId"));
            /*
             * #################################################################
             * ###### # FIN ENREGISTREMENT DES INFORMATIONS DU REPRESENTANT
             * LEGAL #
             * #################################################################
             * ######
             */

            /*
             * #################################################################
             * ###### # ENREGISTREMENT DES INFORMATION DES EMPLOYES #
             * #################################################################
             * ######
             */
            // Recuperations des Caracteristiques Type
            BigDecimal immatriculeCCPF2 = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("employeeRegistrationForm/registeredNationalCcpf/asCurrent").getValue();
            String immatriculeCCPF=immatriculeCCPF2.toString();
            BigDecimal immatriculeAgro2 = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("employeeRegistrationForm/registeredNationalAgro/asCurrent").getValue();
            String immatriculeAgro=immatriculeAgro2.toString();
            String groupeEthique = (String) formBoInstance
                    .getFieldAndMDForPath("employeeRegistrationForm/ethicalGroup/asCurrent").getValue();
//            String dateLivraison = (String) formBoInstance
//                    .getFieldAndMDForPath("employeeRegistrationForm/asCurrent").getValue();
            String lieuDelivraison = (String) formBoInstance
                    .getFieldAndMDForPath("employeeRegistrationForm/place/asCurrent").getValue();
            BigDecimal nbreEnfants2 = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("employeeRegistrationForm/numberOfChildren/asCurrent").getValue();
            String nbreEnfants=nbreEnfants2.toString();
            BigDecimal nbreConjoints2 = (BigDecimal) formBoInstance
                    .getFieldAndMDForPath("employeeRegistrationForm/numberOfSpouse/asCurrent").getValue();
            String nbreConjoints=nbreConjoints2.toString();
            Date dateEntree2 = (Date) formBoInstance
                    .getFieldAndMDForPath("employeeRegistrationForm/dateOfEntry/asCurrent").getValue();
            String dateEntree=dateEntree2.toString();
            List<String> listeCharTypeEmployee=new ArrayList<String>();
            List<String> listeValueCharEmployee=new ArrayList<String>();
            
            // CHAR TYPE CD
            listeCharTypeEmployee.add("IMCCPF");
            listeCharTypeEmployee.add("IMAGRO");
            listeCharTypeEmployee.add("GRPETH");
            //listeCharTypeEmployee.add("DELIVRLE");
            listeCharTypeEmployee.add("DELIVRLA");
            listeCharTypeEmployee.add("NBRENF");
            listeCharTypeEmployee.add("NBRCONJ");
            listeCharTypeEmployee.add("DATENTRE");
            
            // ADHOC VAL
            listeValueCharEmployee.add(immatriculeAgro);
            listeValueCharEmployee.add(immatriculeCCPF);
            listeValueCharEmployee.add(groupeEthique);
            //listeValueCharEmployee.add(dateLivraison);
            listeValueCharEmployee.add(lieuDelivraison);
            listeValueCharEmployee.add(nbreEnfants);
            listeValueCharEmployee.add(nbreConjoints);
            listeValueCharEmployee.add(dateEntree);
            
            
                    personBoInstance = BusinessObjectInstance.create("CM-PersonIndividualDUP");
                    personBoInstance.set("personType", personType);
                    
                    if (listeValueCharEmployee != null) {
                        for (int i = 0; i < listeValueCharEmployee.size(); i++) {
                            if (listeValueCharEmployee.get(i) != "") {
                                COTSInstanceListNode personCharInstance = personBoInstance.getList("personChar").newChild();
                                personCharInstance.set("charTypeCD", listeCharTypeEmployee.get(i));
                                personCharInstance.set("charVal", "");
                                personCharInstance.set("adhocCharVal", listeValueCharEmployee.get(i));
                                personCharInstance.set("searchCharVal", listeValueCharEmployee.get(i));
                                personCharInstance.set("effectiveDate", getSystemDateTime().getDate());
                            }
                        }
                    }
                    
                        // Check if Person Id is provided
                        String firstNameEmployee = (String) formBoInstance
                                .getFieldAndMDForPath("employeeRegistrationForm/firstName/asCurrent").getValue();
                        logger.info("firstNameEmployee: " + firstNameEmployee);
                        String lastNameEmployee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/lastName/asCurrent")
                                .getValue();
                        String sexeEmployee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/sex/asCurrent")
                                .getValue();
                        BigDecimal ninEmployee2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/nin/asCurrent")
                                .getValue();
                        String ninEmployee=ninEmployee2.toString();
                        logger.info("lastNameEmployee: " + lastNameEmployee);
//                        String emailEmployee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/asCurrent")
//                                .getValue();
                        Date dateNaissanceEmployee2 = (Date) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/birthDate/asCurrent")
                                .getValue();
                        String dateNaissanceEmployee=dateNaissanceEmployee2.toString();
                        String lieuNaissanceEmployee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/placeOfBirth/asCurrent")
                                .getValue();
                        
                        
//                        String telephoneEmployee = (String) formBoInstance
//                                .getFieldAndMDForPath("employeeRegistrationForm/asCurrent").getValue();

                        // Set Primary Person Name
                        COTSInstanceListNode personIdInstanceEmployee = personBoInstance.getList("personIds").newChild();
                        personIdInstanceEmployee.set("isPrimaryId", Bool.TRUE);
                        personIdInstanceEmployee.set("idType", "NIN");
                        personIdInstanceEmployee.set("personIdNumber", ninEmployee);
                        COTSInstanceListNode personNameInstanceEmployee = personBoInstance.getList("personName")
                                .newChild();
                        COTSInstanceListNode personPhoneInstance3 = personBoInstance.getList("personPhone").newChild();
                        personNameInstanceEmployee.set("nameType", NameTypeLookup.constants.PRIMARY);
                        personNameInstanceEmployee.set("isPrimaryName", Bool.TRUE);
                        if (notNull(firstNameEmployee) && notNull(lastNameEmployee)) {
                            personNameInstanceEmployee.set("firstName", firstNameEmployee);
                            personNameInstanceEmployee.set("lastName", lastNameEmployee);
                            personPhoneInstance3.set("phoneType", "HOME");
                            personPhoneInstance3.set("phone", "(999) 999-9999");
                        }
                        // ADDRESS
                        ExtendedLookupValue addressType3 = ruleDetails.getExtendedLookupId("addressType").getEntity();
                        // ...
                        // Check if Address Id is provided

                        String regionEmplyee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/region/asCurrent")
                                .getValue();
                        String departementEmplyee = (String) formBoInstance
                                .getFieldAndMDForPath("employeeRegistrationForm/department/asCurrent").getValue();
//                        String communeEmplyee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/asCurrent")
//                                .getValue();
                        String villeEmplyee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/cityTown/asCurrent")
                                .getValue();
                        String quartierEmplyee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/district/asCurrent")
                                .getValue();
                        String adresseEmplyee = (String) formBoInstance.getFieldAndMDForPath("employeeRegistrationForm/address/asCurrent")
                                .getValue();
                        BigDecimal postalEmplyee2 = (BigDecimal) formBoInstance
                                .getFieldAndMDForPath("employeeRegistrationForm/postboxNumber/asCurrent").getValue();
                        String postalEmplyee=postalEmplyee2.toString();
                        BusinessObjectInstance addresBoInstance3 = BusinessObjectInstance.create("C1-Address");

                        logger.info("Creating addresBoInstance create: " + addresBoInstance3.getDocument().asXML());

                        addresBoInstance3.set("bo", addresBoInstance3.getBusinessObject());
                        addresBoInstance3.set("address4", regionEmplyee);
                        addresBoInstance3.set("county", departementEmplyee);
                        addresBoInstance3.set("address1", "Bambey");
                        addresBoInstance3.set("city", villeEmplyee);
                        addresBoInstance3.set("address2", quartierEmplyee);
                        addresBoInstance3.set("address3", adresseEmplyee);
                        addresBoInstance3.set("postal", postalEmplyee);
                        addresBoInstance3 = BusinessObjectDispatcher.add(addresBoInstance3);

                        logger.info("Creating addressId3: " + addresBoInstance3.getString("addressId"));
                        logger.info("Creating addresBoInstance add: " + addresBoInstance3.getDocument().asXML());

                        // Setting Address Information
                        COTSInstanceListNode personAddressInstance3 = personBoInstance.getList("personAddress")
                                .newChild();
                        personAddressInstance3.set("addressId", addresBoInstance3.getString("addressId"));
                        personAddressInstance3.set("addressType", addressType3.getId());
                        personAddressInstance3.set("startDate", getSystemDateTime().getDate());
                        personAddressInstance3.set("deliverable", DeliverableLookup.constants.YES);

                        // FAIRE LA RELATION ENTRE LES DEUX PERSONNES CREEES

                        // RELATION ENTRE PERSONNE 1 ET PERSONNE 3
                        // COTSInstanceListNode personPersonInstance3 =
                        // personBoInstance.getList("personPerson").newChild();
                        // personPersonInstance3.set("personId2", personId1);
                        // personPersonInstance3.set("personId1", personId1);
                        // personPersonInstance3.set("personRelationshipType",
                        // "SIBLING");
                        // personPersonInstance3.set("startDate",
                        // getSystemDateTime().getDate());
                        logger.info("dateSysteme: " + getSystemDateTime().getDate());
                        // RELATION ENTRE PERSONNE 3 ET PERSONNE 1
                        // COTSInstanceListNode personPersonInstance4 =
                        // personBoInstance.getList("personPerson").newChild();
                        // personPersonInstance4.set("personId1", personId1);
                        // personPersonInstance4.set("personId2", personId1);
                        // personPersonInstance4.set("personRelationshipType",
                        // "SPOUSE");
                        // personPersonInstance4.set("startDate",
                        // getSystemDateTime().getDate());
                        // // Invoke BO for Person Creation Legal Entity
                        personBoInstance = BusinessObjectDispatcher.add(personBoInstance);
                        
                        // ------------------------------Insertion des donnees dans la table CM_DMT_HISTORIQUE----------------------------------
                        
                        // -------------------------------Recuperation des donnees-------------------------------------------------------------------
                        String typeMouvement = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/workersMovement/asCurrent")
                                .getValue();
                        Date dateEmbaucheDmt2 = (Date) formBoInstance.getFieldAndMDForPath("employeeContractForm/date/asCurrent")
                                .getValue();
                        BigDecimal ninEmployeeDmt2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("employeeContractForm/nin/asCurrent")
                                .getValue();
                        String nomEmployeeDmt= (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/lastName/asCurrent")
                                .getValue();
                        String prenomEmployeeDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/firstName/asCurrent")
                                .getValue();
                        Date dateEntreeEtsDmt2 = (Date) formBoInstance.getFieldAndMDForPath("employeeContractForm/entryDateOfEstablishment/asCurrent")
                                .getValue();
                        Date dateDeclarationEmbaucheDmt2 = (Date) formBoInstance.getFieldAndMDForPath("employeeContractForm/hiringDeclarationDate/asCurrent")
                                .getValue();
                        String metierDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/profession/asCurrent")
                                .getValue();
                        String emploEtsDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/employmentInTheEstablishment/asCurrent")
                                .getValue();
                        String emploiReferenceDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/referenceJob/asCurrent")
                                .getValue();
                        String convCollectiveDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/collectiveAgreement/asCurrent")
                                .getValue();
                        String categorieDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/category/asCurrent")
                                .getValue();
                        String natureContratDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/natureOfContract/asCurrent")
                                .getValue();
                        Date dateDebutContratDmt2 = (Date) formBoInstance.getFieldAndMDForPath("employeeContractForm/contractStartDate/asCurrent")
                                .getValue();
                        Date dateFinContratDmt2 = (Date) formBoInstance.getFieldAndMDForPath("employeeContractForm/contractEndDate/asCurrent")
                                .getValue();
                        Money salaireContractuelDmt2 = (Money) formBoInstance.getFieldAndMDForPath("employeeContractForm/contractualSalary/asCurrent")
                                .getValue();
                      //  NUMBER salaireContractuelDmt=(NUMBER) salaireContractuelDmt2;
                        BigDecimal pourcentageEmploiDmt2 = (BigDecimal) formBoInstance.getFieldAndMDForPath("employeeContractForm/percentageOfEmployment/asCurrent")
                                .getValue();
                        String employeATMPDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/industryTheEmployeeWillWork/asCurrent")
                                .getValue();
                       String lieuTravailDmt = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/worksite/asCurrent")
                                .getValue();
                       BigDecimal nbreHeureTravail=new BigDecimal(20);
//                        String typeRelationEmp = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/category/asCurrent")
//                                .getValue();
//                        String nbrHeureTravail = (String) formBoInstance.getFieldAndMDForPath("employeeContractForm/category/asCurrent")
//                                .getValue();
                        
                        BusinessObjectInstance histoBoInstance = BusinessObjectInstance
                                .create("CM_DmtHistoriqueBO");
                        histoBoInstance.set("cmTypeMouvement", typeMouvement);
                        histoBoInstance.set("cmDateEmbauche", dateEmbaucheDmt2);
                        histoBoInstance.set("cmIdTravailleur", personBoInstance.getString("personId")); 
                        histoBoInstance.set("cmNIN",ninEmployeeDmt2);
                        histoBoInstance.set("cmNom", nomEmployeeDmt);
                        histoBoInstance.set("cmPrenom", prenomEmployeeDmt);
                        histoBoInstance.set("cmDateEntreeEtablissement",dateEntreeEtsDmt2);
                        histoBoInstance.set("cmDateDeclarationEmbauche", dateDeclarationEmbaucheDmt2);
                        histoBoInstance.set("cmMetier", metierDmt);
                        histoBoInstance.set("cmEmploiEtablissement", emploEtsDmt);
                        histoBoInstance.set("cmEmploiReference", emploiReferenceDmt);
                        histoBoInstance.set("cmConventionCollective", convCollectiveDmt);
                        histoBoInstance.set("cmCategorie", categorieDmt);
                        histoBoInstance.set("cmNatureContrat", natureContratDmt);
                        histoBoInstance.set("cmDateDebutContrat", dateDebutContratDmt2);
                        histoBoInstance.set("cmDateFinContrat", dateFinContratDmt2);
                        histoBoInstance.set("cmSalaireContractuel", salaireContractuelDmt2);
                        histoBoInstance.set("cmPourcentageEmploi", pourcentageEmploiDmt2);
                        histoBoInstance.set("cmEmploiATMP", employeATMPDmt);
                        histoBoInstance.set("cmLieuTravail", lieuTravailDmt);
                        histoBoInstance.set("cmIdEmployeur", personId1);
                        histoBoInstance.set("cmTypeRelationEmp", "Employe");
                        histoBoInstance.set("cmNombreHeureTravail", nbreHeureTravail);
                        histoBoInstance = BusinessObjectDispatcher.add(histoBoInstance);
                        logger.info("histoBoInstance: " + histoBoInstance.getDocument().asXML());
                        // }
                        logger.info("Creating personId3: " + personBoInstance.getString("personId"));
//                        formBoInstance.setXMLFieldStringFromPath("representantlegalid/asCurrent",
//                                personBoInstance.getString("personId"));

                        /*
                         * #####################################################
                         * #### ######## ###### # FIN ENREGISTREMENT DES
                         * INFORMATIS DES EMPLOYES #
                         * #####################################################
                         * #### ######## ######
                         */
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
