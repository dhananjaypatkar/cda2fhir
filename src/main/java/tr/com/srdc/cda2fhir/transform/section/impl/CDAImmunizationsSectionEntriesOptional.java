package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Immunization;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSectionEntriesOptional;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAImmunizationsSectionEntriesOptional implements ICDASection {
	private ImmunizationsSectionEntriesOptional section;

	@SuppressWarnings("unused")
	private CDAImmunizationsSectionEntriesOptional() {
	};

	public CDAImmunizationsSectionEntriesOptional(ImmunizationsSectionEntriesOptional section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<Immunization> transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformImmunizationActivityList(section.getImmunizationActivities(), bundleInfo);
	}
	
	@Override
	public List<ISectionResult> transformAll(IBundleInfo bundleInfo) {
		List<ISectionResult> results = new ArrayList<ISectionResult>();
		results.add(transform(bundleInfo));
		return results;
	}
}
