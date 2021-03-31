package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.openhealthtools.mdht.uml.cda.consol.AllergiesSection;
import org.openhealthtools.mdht.uml.cda.consol.AllergyProblemAct;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.LocalBundleInfo;

public class CDAAllergiesSection implements ICDASection {
	private AllergiesSection section;

	@SuppressWarnings("unused")
	private CDAAllergiesSection() {
	};

	public CDAAllergiesSection(AllergiesSection section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<AllergyIntolerance> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultSingular<AllergyIntolerance> result = SectionResultSingular.getInstance(AllergyIntolerance.class);
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		for (AllergyProblemAct act : section.getAllergyProblemActs()) {
			IEntryResult er = rt.tAllergyProblemAct2AllergyIntolerance(act, localBundleInfo);
			result.updateFrom(er);
			localBundleInfo.updateFrom(er);
		}
		//results.add(result);
		return result;
	}
	
	@Override
	public List<ISectionResult> transformAll(IBundleInfo bundleInfo) {
		List<ISectionResult> results = new ArrayList<ISectionResult>();
		results.add(transform(bundleInfo));
		return results;
	}
}
