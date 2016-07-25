package tr.com.srdc.cda2fhir;

import ca.uhn.fhir.model.dstu2.composite.AnnotationDt;
import ca.uhn.fhir.model.dstu2.composite.AttachmentDt;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.RangeDt;
import ca.uhn.fhir.model.dstu2.composite.RatioDt;
import ca.uhn.fhir.model.primitive.Base64BinaryDt;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.DecimalDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;

import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.hl7.datatypes.BIN;
import org.openhealthtools.mdht.uml.hl7.datatypes.BL;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CV;
import org.openhealthtools.mdht.uml.hl7.datatypes.ED;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.IVL_TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.TEL;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.datatypes.URL;
import org.openhealthtools.mdht.uml.hl7.datatypes.PQ;
import org.openhealthtools.mdht.uml.hl7.datatypes.REAL;
import org.openhealthtools.mdht.uml.hl7.datatypes.RTO;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;

/**
 * Created by mustafa on 7/21/2016.
 */
public interface DataTypesTransformer {

	/* MATURITY LEVELS
	 * 0: not ready to test
	 * 1: needs testing
	 * 2: tested
	 * 3: finalized
	 */
	
    CodingDt CV2Coding(CV cv); /* Maturity Level: 1 */

    CodeableConceptDt CD2CodeableConcept(CD cd); /* Maturity Level: 1 */
    
    BooleanDt BL2Boolean(BL bl); /* Maturity Level: 1 */
    
    DateDt TS2Date(TS ts); /* Maturity Level: 1 */
    
    DecimalDt REAL2Decimal(REAL real); /* Maturity Level: 1 */
    
    StringDt ST2String(ST st); /* Maturity Level: 1 */
    
    UriDt URL2Uri(URL url); /* Maturity Level: 1 */
    
    RatioDt RTO2Ratio(RTO rto); /* Maturity Level: 1 */
    
    PeriodDt IVL_TS2Period(IVL_TS ivlts); /* Maturity Level: 1 */
    
    DateTimeDt TS2DateTime(TS ts); /* Maturity Level: 1 */
    
    QuantityDt PQ2Quantity(PQ pq); /* Maturity Level: 1 */
   
    RangeDt IVL_PQ2Range(IVL_PQ ivlpq); /* Maturity Level: 1 */
    
    AnnotationDt Act2Annotation(Act act); /* Maturity Level: 1 */
    
    ContactPointDt TEL2ContactPoint(TEL tel);  /* Maturity Level: 1 */
    
    IdentifierDt II2Identifier(II ii);  /* Maturity Level: 1 */
    
    HumanNameDt EN2HumanName(EN en);  /* Maturity Level: 1 */
    
    
    AttachmentDt ED2Attachment(ED ed); /*Maturity Level: 1 */
    
    Base64BinaryDt BIN2Base64Binary(BIN bin);/*Maturity Level: 1*/
    
    /*---The datatype SLIST and GLIST does not exist in MDHT----*/
    //SampledDataDt SLITS2SampledData(SLIST slist);
    
}