package tr.com.srdc.cda2fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;

import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.testutil.BundleUtil;
import tr.com.srdc.cda2fhir.testutil.CDAFactories;
import tr.com.srdc.cda2fhir.testutil.generator.ClinicalDocumentMetadataGenerator;
import tr.com.srdc.cda2fhir.transform.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.transform.entry.impl.EntryResult;

public class CompositionTest {
	static ResourceTransformerImpl rt;
	static CDAFactories factories;
	static ClinicalDocumentMetadataGenerator metadataGenerator;
	static String defaultExpectedUse = Config.DEFAULT_IDENTIFIER_USE.getDisplay();
	static String defaultExpectedStatus = Config.DEFAULT_COMPOSITION_STATUS.getDisplay();
	static String defaultExpectedAssigner = ClinicalDocumentMetadataGenerator.DEFAULT_ASSN_AUTH;
	static String defaultExpectedIdValue = ClinicalDocumentMetadataGenerator.DEFAULT_ID_ROOT;
	static String defaultExpectedTypeCode = ClinicalDocumentMetadataGenerator.DEFAULT_CODE_CODE;
	static String defaultExpectedTypeSystem = "http://loinc.org";
	static String defaultExpectedTypeDisplay = ClinicalDocumentMetadataGenerator.DEFAULT_CODE_DISPLAY;
	static String defaultExpectedTitle = ClinicalDocumentMetadataGenerator.DEFAULT_TITLE;
	static String defaultExpectedConfidentiality = "N";
	static String defaultExpectedPeriodStart = "1970-09-19";
	static String defaultExpectedPeriodEnd = "2019-02-14";
	static String defaultExpectedEventCodeText = "Primary Care Physician";

	@BeforeClass
	public static void init() {
		CDAUtil.loadPackages();
		rt = new ResourceTransformerImpl();
		factories = CDAFactories.init();
		metadataGenerator = new ClinicalDocumentMetadataGenerator();
	}

	@Test
	public void testComposition() throws Exception {

		ClinicalDocument clinicalDoc = metadataGenerator.generateClinicalDoc(factories);
		EntryResult entryResult = rt.tClinicalDocument2Composition(clinicalDoc);
		Bundle bundle = entryResult.getBundle();
		Composition comp = BundleUtil.findOneResource(bundle, Composition.class);
		Practitioner primary = BundleUtil.findOneResource(bundle, Practitioner.class);

		Assert.assertEquals("Expect assigner to equal assigningAuthorityName", defaultExpectedAssigner,
				comp.getIdentifier().getAssigner().getDisplay());
		Assert.assertEquals("Expect use to equal default", defaultExpectedUse,
				comp.getIdentifier().getUse().getDisplay());
		Assert.assertEquals("Expect status to equal default", defaultExpectedStatus, comp.getStatus().getDisplay());
		Assert.assertEquals("Expect Identifier Value to equal Clinical Document id root", defaultExpectedIdValue,
				comp.getIdentifier().getValue());
		Assert.assertEquals("Expect type.code to equal code.code", defaultExpectedTypeCode,
				comp.getType().getCodingFirstRep().getCode());
		Assert.assertEquals("Expect type.system code.codeSystemName", defaultExpectedTypeSystem,
				comp.getType().getCodingFirstRep().getSystem());
		Assert.assertEquals("Expect type.display code.displayName", defaultExpectedTypeDisplay,
				comp.getType().getCodingFirstRep().getDisplay());
		Assert.assertEquals("Expect title to equal title", defaultExpectedTitle, comp.getTitle());
		Assert.assertEquals("Expect confidentiality to equal confidentiality", defaultExpectedConfidentiality,
				comp.getConfidentiality().toString());
		Assert.assertEquals("Expect composition to code system to equal http://hl7.org/fhir/ValueSet/doc-classcodes",
				"http://hl7.org/fhir/ValueSet/doc-classcodes", comp.getCategoryFirstRep().getCodingFirstRep().getSystem());
		Assert.assertEquals("Expect composition to code to equal LP173418-7", "LP173421-7",
				comp.getCategoryFirstRep().getCodingFirstRep().getCode());
		Assert.assertEquals("Expect composition to code display to equal Note", "Note",
				comp.getCategoryFirstRep().getCodingFirstRep().getDisplay());
		Assert.assertEquals("Expect composition event.detail to contain reference to prmary care physician",
				comp.getEvent().get(0).getDetailFirstRep().getReference(), primary.getId());
		Assert.assertEquals("Expect composition event.period.start to equal service Event effective time low",
				defaultExpectedPeriodStart, comp.getEvent().get(0).getPeriod().getStartElement().getValueAsString());
		Assert.assertEquals("Expect composition event.period.end to equal service Event effective time high",
				defaultExpectedPeriodEnd, comp.getEvent().get(0).getPeriod().getEndElement().getValueAsString());
	}
}
