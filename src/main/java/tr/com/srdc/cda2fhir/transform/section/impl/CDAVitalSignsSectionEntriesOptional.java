package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Observation;
import org.openhealthtools.mdht.uml.cda.consol.VitalSignsSectionEntriesOptional;

import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;

public class CDAVitalSignsSectionEntriesOptional implements ICDASection {
	private VitalSignsSectionEntriesOptional section;

	@SuppressWarnings("unused")
	private CDAVitalSignsSectionEntriesOptional() {
	};

	public CDAVitalSignsSectionEntriesOptional(VitalSignsSectionEntriesOptional section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<Observation> transform(IBundleInfo bundleInfo) {
		return CDASectionCommon.transformVitalSignsOrganizerList(section.getVitalSignsOrganizers(), bundleInfo);
	}
	
	@Override
	public List<ISectionResult> transformAll(IBundleInfo bundleInfo) {
		List<ISectionResult> results = new ArrayList<ISectionResult>();
		results.add(transform(bundleInfo));
		return results;
	}
}
