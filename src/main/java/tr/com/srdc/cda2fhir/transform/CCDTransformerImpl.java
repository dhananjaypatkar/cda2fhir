package tr.com.srdc.cda2fhir.transform;

import java.io.FileInputStream;

/*
 * #%L
 * CDA to FHIR Transformer Library
 * %%
 * Copyright (C) 2016 SRDC Yazilim Arastirma ve Gelistirme ve Danismanlik Tic. A.S.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.SectionComponent;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.consol.ConsolPackage;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.primitive.StringDt;
import tr.com.srdc.cda2fhir.conf.Config;
import tr.com.srdc.cda2fhir.transform.entry.IEntryResult;
import tr.com.srdc.cda2fhir.transform.section.CDASectionTypeEnum;
import tr.com.srdc.cda2fhir.transform.section.ICDASection;
import tr.com.srdc.cda2fhir.transform.section.ISectionResult;
import tr.com.srdc.cda2fhir.transform.util.IDeferredReference;
import tr.com.srdc.cda2fhir.transform.util.IIdentifierMap;
import tr.com.srdc.cda2fhir.transform.util.IdentifierMapFactory;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleInfo;
import tr.com.srdc.cda2fhir.transform.util.impl.BundleRequest;
import tr.com.srdc.cda2fhir.transform.util.impl.ReferenceInfo;
import tr.com.srdc.cda2fhir.util.EMFUtil;
import tr.com.srdc.cda2fhir.util.FHIRUtil;
import tr.com.srdc.cda2fhir.util.IdGeneratorEnum;

public class CCDTransformerImpl implements ICDATransformer, Serializable {

	private static final long serialVersionUID = 1L;

	private int counter;
	private IdGeneratorEnum idGenerator;
	private IResourceTransformer resTransformer;
	private Reference patientRef;

	private List<CDASectionTypeEnum> supportedSectionTypes = new ArrayList<CDASectionTypeEnum>();

	private final Logger logger = LoggerFactory.getLogger(CCDTransformerImpl.class);

	/**
	 * Default constructor that initiates with a UUID resource id generator
	 */
	public CCDTransformerImpl() {
		this.counter = 0;
		// The default resource id pattern is UUID
		this.idGenerator = IdGeneratorEnum.UUID;
		resTransformer = new ResourceTransformerImpl(this);
		this.patientRef = null; // TODO: Not thread safe?

		supportedSectionTypes.add(CDASectionTypeEnum.ALLERGIES_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.IMMUNIZATIONS_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.MEDICATIONS_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.PROBLEM_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.PROCEDURES_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.ENCOUNTERS_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.ENCOUNTERS_SECTION_ENTRIES_OPTIONAL);
		supportedSectionTypes.add(CDASectionTypeEnum.RESULTS_SECTION);
		supportedSectionTypes.add(CDASectionTypeEnum.VITAL_SIGNS_SECTION);
	}

	/**
	 * Constructor that initiates with the provided resource id generator
	 *
	 * @param idGen The id generator enumeration to be set
	 */
	public CCDTransformerImpl(IdGeneratorEnum idGen) {
		this();
		// Override the default resource id pattern
		this.idGenerator = idGen;
	}

	@Override
	public Reference getPatientRef() {
		return patientRef;
	}

	public void setPatientRef(Reference patientRef) {
		this.patientRef = patientRef;
	}

	public void setResourceTransformer(IResourceTransformer resTransformer) {
		this.resTransformer = resTransformer;
	}

	@Override
	public synchronized String getUniqueId() {
		switch (this.idGenerator) {
		case COUNTER:
			return Integer.toString(++counter);
		case UUID:
		default:
			return UUID.randomUUID().toString();
		}
	}

	@Override
	public void setIdGenerator(IdGeneratorEnum idGen) {
		this.idGenerator = idGen;
	}

	public void addSection(CDASectionTypeEnum sectionEnum) {
		supportedSectionTypes.add(sectionEnum);
	}

	public void setSection(CDASectionTypeEnum sectionEnum) {
		supportedSectionTypes.clear();
		supportedSectionTypes.add(sectionEnum);
	}

	/**
	 * @param cda                A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                           Document (CCD) instance to be transformed
	 * @param bundleType         Desired type of the FHIR Bundle to be returned
	 *
	 * @param patientRef         Patient Reference of the given CDA Document
	 *
	 * @param resourceProfileMap The mappings of default resource profiles to
	 *                           desired resource profiles. Used to set profile
	 *                           URI's of bundle entries or omit unwanted entries.
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources but Patient that are referenced
	 *         within the Composition.
	 */
	public Bundle createTransactionBundle(Bundle bundle, Map<String, String> resourceProfileMap, boolean addURLs) {
		Bundle resultBundle = new Bundle();
		resultBundle.setType(BundleType.TRANSACTION);

		for (BundleEntryComponent entry : bundle.getEntry()) {
			// Patient resource will not be added
			if (entry != null) {
				// Add request and fullUrl fields to entries
				BundleRequest.addRequestToEntry(entry);
				if (addURLs) {
					addFullUrlToEntry(entry);
				}
				// if resourceProfileMap is specified omit the resources with no profiles given
				// Empty profileUri means add with no change
				if (resourceProfileMap != null) {
					String profileUri = resourceProfileMap.get(entry.getResource().getResourceType().name());
					if (profileUri != null) {
						if (!profileUri.isEmpty()) {
							entry.getResource().getMeta().addProfile(profileUri);
						}
						resultBundle.addEntry(entry);
					}
				} else {
					resultBundle.addEntry(entry);
				}
			}
		}

		return resultBundle;
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param cda                A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                           Document (CCD) instance to be transformed
	 * @param bundleType         The type of bundle to create, currently only
	 *                           supports transaction bundles.
	 * @param resourceProfileMap The mappings of default resource profiles to
	 *                           desired resource profiles. Used to set profile
	 *                           URI's of bundle entries or omit unwanted entries.
	 * @param documentBody       The decoded documentBody of the document, to be
	 *                           included in a provenance object.
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 * @throws Exception
	 */

	public Bundle transformDocument(String filePath, BundleType bundleType, Map<String, String> resourceProfileMap,
			String documentBody, Identifier assemblerDevice) throws Exception {
		ContinuityOfCareDocument cda = getClinicalDocument(filePath);
		if (bundleType.equals(BundleType.TRANSACTION)) {
			Config.setIsTransactionBundle(true);
		}
		Bundle bundle = transformDocument(cda, true);
		bundle.setType(bundleType);
		if (assemblerDevice != null && !StringUtils.isEmpty(documentBody)) {
			bundle = resTransformer.tProvenance(bundle, documentBody, assemblerDevice);
		}

		if (bundleType.equals(BundleType.TRANSACTION)) {
			return createTransactionBundle(bundle, resourceProfileMap, false);
		}
		return bundle;
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param filePath A file path string to a Consolidated CDA (C-CDA) 2.1
	 *                 Continuity of Care Document (CCD) on file system
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 * @throws Exception
	 */
	public Bundle transformDocument(String filePath) throws Exception {
		ContinuityOfCareDocument cda = getClinicalDocument(filePath);
		return transformDocument(cda, true);
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param cda A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 * @param cda A Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 *            instance to be transformed
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 */

	public Bundle transformDocument(ContinuityOfCareDocument cda) {
		return transformDocument(cda, true);
	}

	/**
	 * @param cda                A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                           fhir-r4 Document (CCD) instance to be transformed
	 * @param bundleType         The type of bundle to create, currently only
	 *                           supports transaction bundles.
	 * @param resourceProfileMap The mappings of default resource profiles to
	 *                           desired resource profiles. Used to set profile
	 *                           URI's of bundle entries or omit unwanted entries.
	 * @param documentBody       The decoded base64 document that would be included
	 *                           in the provenance object if provided.
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 * @throws Exception
	 */

	@Override
	public Bundle transformDocument(ContinuityOfCareDocument cda, BundleType bundleType,
			Map<String, String> resourceProfileMap, String documentBody, Identifier assemblerDevice) throws Exception {
		if (bundleType.equals(BundleType.TRANSACTION)) {
			Config.setIsTransactionBundle(true);
		}
		Bundle bundle = transformDocument(cda, true);
		bundle.setType(bundleType);
		if (assemblerDevice != null && !StringUtils.isEmpty(documentBody)) {
			bundle = resTransformer.tProvenance(bundle, documentBody, assemblerDevice);
		}

		if (bundleType.equals(BundleType.TRANSACTION)) {
			return createTransactionBundle(bundle, resourceProfileMap, false);
		}
		return bundle;
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param cda          A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                     Document (CCD) instance to be transformed.
	 * @param documentBody The decoded base64 document that would be included in the
	 *                     provenance object if provided.
	 * @return A FHIR Bundle that contains a Composition corresponding to the CCD
	 *         document and all other resources that are referenced within the
	 *         Composition.
	 */
	@Override
	public Bundle transformDocument(ContinuityOfCareDocument cda, String documentBody, Identifier assemblerDevice) {
		Bundle bundle = transformDocument(cda, true);
		if (assemblerDevice != null & !StringUtils.isEmpty(documentBody)) {
			bundle = resTransformer.tProvenance(bundle, documentBody, assemblerDevice);
		}
		return bundle;
	}

	private ICDASection findCDASection(Section section) {
		for (CDASectionTypeEnum sectionType : supportedSectionTypes) {
			if (sectionType.supports(section)) {
				return sectionType.toCDASection(section);
			}
		}
		logger.info("Encountered unsupported section: " + section.getTitle().getText());
		return null;
	}

	/**
	 * Transforms a Consolidated CDA (C-CDA) 2.1 Continuity of Care Document (CCD)
	 * instance to a Bundle of corresponding FHIR resources
	 *
	 * @param cda                A Consolidated CDA (C-CDA) 2.1 Continuity of Care
	 *                           Document (CCD) instance to be transformed
	 * @param includeComposition Flag to include composition (required for document
	 *                           type bundles)
	 * @return A FHIR Bundle
	 */
	public Bundle transformDocument(ContinuityOfCareDocument ccd, boolean includeComposition) { // TODO: Should be
																								// bundle type based.
		if (ccd == null) {
			return null;
		}

		// init the global ccd bundle via a call to resource transformer, which handles
		// cda header data (in fact, all except the sections)
		IEntryResult entryResult = resTransformer.tClinicalDocument2Bundle(ccd, includeComposition);
		Bundle ccdBundle = entryResult.getBundle();
		if (ccdBundle == null) {
			ccdBundle = new Bundle();
		}

		// the first bundle entry is always the composition
		Composition ccdComposition = includeComposition ? (Composition) ccdBundle.getEntry().get(0).getResource()
				: null;

		// init the patient id reference if it is not given externally.
		if (patientRef == null) {
			List<Patient> patients = FHIRUtil.findResources(ccdBundle, Patient.class);
			if (patients.size() > 0) {
				patientRef = new Reference(patients.get(0).getId());
				String referenceString = ReferenceInfo.getDisplay(patients.get(0));
				if (referenceString != null) {
					patientRef.setDisplay(referenceString);
				}
			}
		} else if (ccdComposition != null) { // Correct the subject at composition with given patient reference.
			ccdComposition.setSubject(patientRef);
		}

		BundleInfo bundleInfo = new BundleInfo(resTransformer);
		bundleInfo.updateFrom(entryResult);
		List<IDeferredReference> deferredReferences = new ArrayList<IDeferredReference>();

		// transform the sections
		for (Section cdaSec : ccd.getSections()) {
			ICDASection section = findCDASection(cdaSec);
			if (section != null) {
				SectionComponent fhirSec = resTransformer.tSection2Section(cdaSec);

				if (fhirSec == null) {
					continue;
				}
				
				if (ccdComposition != null) {
					ccdComposition.addSection(fhirSec);
				}

				// add text annotation lookups.
				if (cdaSec.getText() != null) {
					Map<String, String> idedAnnotations = EMFUtil.findReferences(cdaSec.getText());
					bundleInfo.mergeIdedAnnotations(idedAnnotations);
				}

				//ISectionResult sectionResult = section.transform(bundleInfo);
				List<ISectionResult> sectionResults = section.transformAll(bundleInfo);
				//sectionResults.stream().forEach(sectionResult -> {
				for (ISectionResult sectionResult : sectionResults) {
					if (sectionResult != null) {
						FHIRUtil.mergeBundle(sectionResult.getBundle(), ccdBundle);
						if (fhirSec != null) {
							List<? extends Resource> resources = sectionResult.getSectionResources();
							for (Resource resource : resources) {
								Reference ref = fhirSec.addEntry();
								ref.setReference(resource.getId());
								String referenceString = ReferenceInfo.getDisplay(resource);
								if (referenceString != null) {
									ref.setDisplay(referenceString);
								}
							}
						}
						if (sectionResult.hasDeferredReferences()) {
							deferredReferences.addAll(sectionResult.getDeferredReferences());
						}
						bundleInfo.updateFrom(sectionResult);
					}
				}
					
				//});
				
			}
		}

		IIdentifierMap<String> identifierMap = IdentifierMapFactory.bundleToIds(ccdBundle);

		// deferred references only present for procedure encounters.
		if (!deferredReferences.isEmpty()) {
			for (IDeferredReference dr : deferredReferences) {
				String id = identifierMap.get(dr.getFhirType(), dr.getIdentifier());
				if (id != null) {
					Reference reference = new Reference(id);
					String referenceString = ReferenceInfo.getDisplay(dr.getResource());
					if (referenceString != null) {
						reference.setDisplay(referenceString);
					}
					dr.resolve(reference);
				} else {
					String msg = String.format("%s %s is referred but not found", dr.getFhirType(),
							dr.getIdentifier().getValue());
					logger.error(msg);
				}
			}
		}

		return ccdBundle;
	}

	/**
	 * Adds fullUrl field to the entry using it's resource id.
	 *
	 * @param entry Entry which fullUrl field to be added.
	 */
	private void addFullUrlToEntry(BundleEntryComponent entry) {
		// entry.setFullUrl("urn:uuid:" + entry.getResource().getId().getIdPart());
		entry.setFullUrl("urn:uuid:" + entry.getResource().getIdElement().getIdPart());
	}

	private ContinuityOfCareDocument getClinicalDocument(String filePath) throws Exception {
		FileInputStream fis = new FileInputStream(filePath);
		// ClinicalDocument cda = CDAUtil.load(fis);
		ContinuityOfCareDocument cda = (ContinuityOfCareDocument) CDAUtil.loadAs(fis,
				ConsolPackage.eINSTANCE.getContinuityOfCareDocument());
		fis.close();
		return cda;
	}
}
