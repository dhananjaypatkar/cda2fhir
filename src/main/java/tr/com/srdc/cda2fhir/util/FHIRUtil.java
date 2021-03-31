package tr.com.srdc.cda2fhir.util;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import tr.com.srdc.cda2fhir.conf.Config;

public class FHIRUtil {

	private static IParser jsonParser;
	private static IParser xmlParser;

	private final static Logger logger = LoggerFactory.getLogger(FHIRUtil.class);

	static {
		jsonParser = Config.getFhirContext().newJsonParser();
		xmlParser = Config.getFhirContext().newXmlParser();
		jsonParser.setPrettyPrint(true);
		xmlParser.setPrettyPrint(true);
	}

	public static String encodeToJSON(IBaseResource res) {
		return jsonParser.encodeResourceToString(res);
	}

	public static <T extends IBaseResource> String encodeToJSON(Collection<T> resources) {
		String[] objects = resources.stream().map(r -> encodeToJSON(r)).toArray(String[]::new);
		return "[" + String.join(", ", objects) + "]";
	}

	public static String encodeToXML(IBaseResource res) {
		return xmlParser.encodeResourceToString(res);
	}

	public static void printJSON(IBaseResource res) {
		System.out.println(jsonParser.encodeResourceToString(res));
	}

	public static <T extends IBaseResource> void printJSON(Collection<T> resources) {
		System.out.println(encodeToJSON(resources));
	}

	public static void printXML(IBaseResource res) {
		System.out.println(xmlParser.encodeResourceToString(res));
	}

	public static void printJSON(IBaseResource res, String filePath) throws IOException {
		File f = new File(filePath);
		f.getParentFile().mkdirs();
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			jsonParser.encodeResourceToWriter(res, fw);
		} catch (IOException ie) {
			logger.error("Could not print FHIR JSON to file", ie);
			throw new IOException(ie);
		} catch (DataFormatException de) {
			logger.error("Could not print FHIR JSON to file", de);
			throw new DataFormatException(de);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException ie) {
					logger.error("Could not close writer for function \"printJSON.\" with IBaseResource.");
				}
			}
		}
	}

	public static <T extends IBaseResource> void printJSON(Collection<T> resources, String filePath)
			throws IOException {
		File f = new File(filePath);
		f.getParentFile().mkdirs();
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			String json = encodeToJSON(resources);
			fw.write(json);
		} catch (IOException e) {
			logger.error("Could not print FHIR JSON to file", e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException ie) {
					logger.error("Could not close writer for function \"printJSON.\" with Collection.");
				}
			}
		}
	}

	public static void printXML(IBaseResource res, String filePath) throws IOException {
		File f = new File(filePath);
		f.getParentFile().mkdirs();
		FileWriter fw = null;
		try {
			fw = new FileWriter(f);
			xmlParser.encodeResourceToWriter(res, fw);
		} catch (IOException e) {
			logger.error("Could not print FHIR XML to file", e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException ie) {
					logger.error("Could not close writer for function \"printJSON.\" with Collection.");
				}
			}
		}
	}

	public static void printJSON(IBaseResource res, Writer writer) {
		try {
			jsonParser.encodeResourceToWriter(res, writer);
		} catch (IOException e) {
			logger.error("Could not print FHIR JSON to writer", e);
		}
	}

	public static void printXML(IBaseResource res, Writer writer) {
		try {
			xmlParser.encodeResourceToWriter(res, writer);
		} catch (IOException e) {
			logger.error("Could not print FHIR XML to writer", e);
		}
	}

	public static <T extends Resource> List<T> findResources(Bundle bundle, Class<T> type) {
		return bundle.getEntry().stream().map(b -> b.getResource()).filter(r -> type.isInstance(r))
				.map(r -> type.cast(r)).collect(Collectors.toList());
	}

	public static <T extends Resource> T findFirstResource(Bundle bundle, Class<T> type) {
		Optional<T> result = bundle.getEntry().stream().map(b -> b.getResource()).filter(r -> type.isInstance(r))
				.map(r -> type.cast(r)).findFirst();
		return result.orElse(null);

	}

	public static void mergeBundle(Bundle source, Bundle target) {
		if (source == null || target == null) {
			return;
		}

		for (BundleEntryComponent entry : source.getEntry()) {
			if (entry != null) {
				target.addEntry(entry);
			}
		}
	}

	public static Map<String, Resource> getIdResourceMap(Bundle bundle) {
		Map<String, Resource> result = new HashMap<String, Resource>();
		bundle.getEntry().stream().map(e -> e.getResource()).forEach(r -> result.put(r.getId(), r));
		return result;
	}

	interface ResourcePredicate {
		boolean get(Resource resource);
	}

	public static Bundle bundleJSON(File file) throws IOException {
		InputStream targetStream = new FileInputStream(file);
		Bundle resultBundle = (Bundle) jsonParser.parseResource(targetStream);
		targetStream.close();
		return resultBundle;
	}

	public static String toCDADatetime(String fhirDatetime) {
		String noColon = fhirDatetime.replace(":", "");
		String[] pieces = noColon.split("T");
		String result = pieces[0].replace("-", "");
		if (pieces.length > 1) {
			String timezone = null;
			if (pieces[1].indexOf('-') >= 0) {
				String[] pieces2 = pieces[1].split("-");
				result += pieces2[0].replace(":", "");
				timezone = "-" + pieces2[1];
			} else if (pieces[1].indexOf('+') >= 0) {
				String[] pieces2 = pieces[1].split("+");
				result += pieces2[0].replace(":", "");
				timezone = "+" + pieces2[1];
			}
			if (timezone != null) {
				result += timezone;
			}
		}
		return result;
	}

	public static String toFHIRDatetime(String cdaDateTime) {
		if (cdaDateTime.length() < 4) {
			return null;
		}
		String[] pieces = cdaDateTime.split("-");
		String datetime = pieces[0];
		int length = datetime.length();
		String result = datetime.substring(0, 4);
		if (length > 5) {
			result += "-" + datetime.substring(4, 6);
			if (length > 7) {
				result += "-" + datetime.substring(6, 8);
				if (length > 11) {
					result += "T" + datetime.substring(8, 10) + ":" + datetime.substring(10, 12);
					if (length > 13) {
						result += ":" + datetime.substring(12, 14);
					} else {
						result += ":00";
					}
				}
			}
		}
		String zone = pieces.length > 1 ? pieces[1] : null;
		if (zone != null && zone.length() > 0) {
			result += "-" + zone.substring(0, 2) + ":" + zone.substring(2, 4);
		}
		return result;
	}
}
