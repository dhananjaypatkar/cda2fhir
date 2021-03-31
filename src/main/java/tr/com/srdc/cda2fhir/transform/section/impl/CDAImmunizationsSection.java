package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Immunization;
import org.openhealthtools.mdht.uml.cda.consol.ImmunizationsSection;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAImmunizationsSection implements ICDASection {
	private ImmunizationsSection section;

	@SuppressWarnings("unused")
	private CDAImmunizationsSection() {
	};

	public CDAImmunizationsSection(ImmunizationsSection section) {
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
