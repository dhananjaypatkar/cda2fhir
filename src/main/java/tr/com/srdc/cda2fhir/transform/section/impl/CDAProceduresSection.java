package tr.com.srdc.cda2fhir.transform.section.impl;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.Procedure;
import org.openhealthtools.mdht.uml.cda.consol.ProcedureActivityProcedure;
import org.openhealthtools.mdht.uml.cda.consol.ProceduresSection;

import tr.com.srdc.cda2fhir.transform.IResourceTransformer;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IBundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.LocalBundleInfo;

public class CDAProceduresSection implements ICDASection {
	private ProceduresSection section;

	@SuppressWarnings("unused")
	private CDAProceduresSection() {
	};

	public CDAProceduresSection(ProceduresSection section) {
		this.section = section;
	}

	@Override
	public SectionResultSingular<Procedure> transform(IBundleInfo bundleInfo) {
		IResourceTransformer rt = bundleInfo.getResourceTransformer();
		SectionResultSingular<Procedure> result = SectionResultSingular.getInstance(Procedure.class);
		LocalBundleInfo localBundleInfo = new LocalBundleInfo(bundleInfo);
		for (ProcedureActivityProcedure act : section.getConsolProcedureActivityProcedures()) {
			IEntryResult er = rt.tProcedure2Procedure(act, localBundleInfo);
			result.updateFrom(er);
			localBundleInfo.updateFrom(er);
		}
		return result;
	}
	
	@Override
	public List<ISectionResult> transformAll(IBundleInfo bundleInfo) {
		List<ISectionResult> results = new ArrayList<ISectionResult>();
		results.add(transform(bundleInfo));
		return results;
	}
}
