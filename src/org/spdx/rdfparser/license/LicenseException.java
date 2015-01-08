/**
 * Copyright (c) 2014 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.rdfparser.license;

import java.util.ArrayList;

import org.spdx.compare.LicenseCompareHelper;
import org.spdx.rdfparser.InvalidSPDXAnalysisException;
import org.spdx.rdfparser.RdfParserHelper;
import org.spdx.rdfparser.SpdxRdfConstants;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Represents an SPDX license exception as defined in the License Expression Language
 * Used with the "with" unary expression.
 * 
 * @author Gary O'Neall
 *
 */
public class LicenseException  {
	
	Model model = null;
	Node exceptionNode = null;
	Resource resource = null;
	
	private String licenseExceptionId;
	private String name;
	private String licenseExceptionText;
	private String[] seeAlso;
	private String comment;
	private String example;
	
	public LicenseException(Model model, Node node) throws InvalidSPDXAnalysisException {
		this.model = model;
		this.exceptionNode = node;
		resource = RdfParserHelper.convertToResource(model, exceptionNode);
		// fill in the local property cache
		// licenseExceptionId
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION_ID).asNode();
		Triple m = Triple.createMatch(exceptionNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseExceptionId = t.getObject().toString(false);
		}
		// name
		this.name = null;
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_NAME).asNode();
		m = Triple.createMatch(exceptionNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.name = t.getObject().toString(false);
		}
		// licenseExceptionText
		this.licenseExceptionText = null;
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXCEPTION_TEXT).asNode();
		m = Triple.createMatch(exceptionNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.licenseExceptionText = t.getObject().toString(false);
		}
		// seeAlso
		ArrayList<String> alsourceUrls = new ArrayList<String>();
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_SEE_ALSO).asNode();
		m = Triple.createMatch(exceptionNode, p, null);
		tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			alsourceUrls.add(t.getObject().toString(false));
		}
		this.seeAlso = alsourceUrls.toArray(new String[alsourceUrls.size()]);
		// comments
		p = model.getProperty(SpdxRdfConstants.RDFS_NAMESPACE, SpdxRdfConstants.RDFS_PROP_COMMENT).asNode();
		m = Triple.createMatch(exceptionNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (!tripleIter.hasNext()) {
			// check the old property name for compatibility with pre-1.1 generated RDF files
			p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_STD_LICENSE_NOTES_VERSION_1).asNode();
			m = Triple.createMatch(exceptionNode, p, null);
			tripleIter = model.getGraph().find(m);	
		}
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.comment = t.getObject().toString(false);
		} else {
			this.comment = null;
		}
		// example
		this.example = null;
		p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXAMPLE).asNode();
		m = Triple.createMatch(exceptionNode, p, null);
		tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			this.example = t.getObject().toString(false);
		}
	}

	/**
	 * @param licenseExceptionId Exception ID - short form ID
	 * @param name Full name of the Exception
	 * @param licenseExceptionText Text for the Exception
	 * @param comment Comments on the exception
	 * @param example Example of use
	 * @param seeAlso URL references to external sources for the exception
	 */
	public LicenseException(String licenseExceptionId, String name, String licenseExceptionText,
			String[] seeAlso, String comment, String example) {
		this.licenseExceptionId = licenseExceptionId;
		this.name = name;
		this.licenseExceptionText = licenseExceptionText;
		this.seeAlso = seeAlso;
		this.comment = comment;
		this.example = example;
	}
	
	/**
	 * @param licenseExceptionId Exception ID - short form ID
	 * @param name Full name of the Exception
	 * @param licenseExceptionText Text for the Exception
	 */
	public LicenseException(String licenseExceptionId, String name, String licenseExceptionText) {
		this(licenseExceptionId, name, licenseExceptionText, new String[0], "", "");
	}
	
	public LicenseException() {
		this(null, null, null);
	}
	
	/**
	 * If a resource does not already exist in this model for this object,
	 * create a new resource and populate it.  If the resource does exist,
	 * return the existing resource.
	 * @param model
	 * @return resource created from the model
	 * @throws InvalidSPDXAnalysisException 
	 */
	protected Resource createResource(Model model, String uri) throws InvalidSPDXAnalysisException {	
		if (this.model != null &&
				this.exceptionNode != null &&
				this.resource != null &&
				(this.model.equals(model) || (this.exceptionNode.isURI()))) {
			return resource;
		} else {
			this.model = model;
			this.exceptionNode = findException(model, this.licenseExceptionId);
			this.resource = null;
			if (this.exceptionNode != null) {	// found an existing exception in the model
				if (this.exceptionNode.isURI()) {
					this.resource = model.createResource(this.exceptionNode.getURI());
				} else if (this.exceptionNode.isBlank()) {
					this.resource = model.createResource(this.exceptionNode.getBlankNodeId());
				}
			} else {	// create a node
				Resource type = model.createResource(SpdxRdfConstants.SPDX_NAMESPACE + SpdxRdfConstants.CLASS_SPDX_LICENSE_EXCEPTION);
				if (uri == null || uri.isEmpty()) {
					this.resource = model.createResource(type);
				} else {
					this.resource = model.createResource(uri, type);
				}
			}
			// check to make sure we are not overwriting an existing exception with the same ID
			if (this.exceptionNode != null) {
			String existingExceptionText = getExceptionTextFromModel(model, this.exceptionNode);
				if (existingExceptionText != null && this.licenseExceptionText != null) {
					if (!LicenseCompareHelper.isLicenseTextEquivalent(existingExceptionText, this.licenseExceptionText)) {
						throw(new DuplicateExtractedLicenseIdException("License exception ID "+this.licenseExceptionId+" already exists.  Can not add a license restriciton with the same ID but different text."));
					}
				}
			}
			// add the properties
			// licenseExceptionId
			if (this.licenseExceptionId != null) {
				Property idProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_LICENSE_EXCEPTION_ID);
				model.removeAll(this.resource, idProperty, null);
				this.resource.addProperty(idProperty,  this.licenseExceptionId);
			}
			// name
			if (this.name != null) {
				Property nameProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_NAME);
				model.removeAll(this.resource, nameProperty, null);
				this.resource.addProperty(nameProperty, this.name);
			}
			// comment
			if (this.comment != null) {
				Property commentProperty = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, 
						SpdxRdfConstants.RDFS_PROP_COMMENT);
				model.removeAll(this.resource, commentProperty, null);
				this.resource.addProperty(commentProperty, this.comment);
			}
			// example
			if (this.example != null) {
				Property exampleProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_EXAMPLE);
				model.removeAll(this.resource, exampleProperty, null);
				this.resource.addProperty(exampleProperty, this.example);
			}
			// seeAlso
			if (this.seeAlso != null && this.seeAlso.length > 0) {
				Property seeAlsoProperty = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, 
						SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
				model.removeAll(this.resource, seeAlsoProperty, null);
				for (int i = 0; i < this.seeAlso.length; i++) {
					this.resource.addProperty(seeAlsoProperty, this.seeAlso[i]);
				}
			}
			// licenseExceptionText
			if (this.licenseExceptionText != null) {
				Property textProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
						SpdxRdfConstants.PROP_EXCEPTION_TEXT);
				model.removeAll(this.resource, textProperty, null);
				this.resource.addProperty(textProperty, this.licenseExceptionText);
			}
			return this.resource;
		}
	}

	/**
	 * Get the exception text from the model
	 * @param model
	 * @param exceptionNode
	 * @return
	 */
	public static String getExceptionTextFromModel(Model model, Node exceptionNode) {
		Node p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXCEPTION_TEXT).asNode();
		Triple m = Triple.createMatch(exceptionNode, p, null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		if (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			return t.getObject().toString(false);
		}
		return null;
	}

	/**
	 * Searches the model for a exception with the ID
	 * @param model
	 * @param id
	 * @return Node containing the exception or Null if none found
	 */
	public static Node findException(Model model, String id) {
		Property idProperty = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, 
				SpdxRdfConstants.PROP_LICENSE_EXCEPTION_ID);
		Property typeProperty = model.getProperty(SpdxRdfConstants.RDF_NAMESPACE, 
				SpdxRdfConstants.RDF_PROP_TYPE);
		Property exceptionTypeProperty = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE,
				SpdxRdfConstants.CLASS_SPDX_LICENSE_EXCEPTION);
		Triple m = Triple.createMatch(null, idProperty.asNode(), null);
		ExtendedIterator<Triple> tripleIter = model.getGraph().find(m);	
		while (tripleIter.hasNext()) {
			Triple t = tripleIter.next();
			if (t.getObject().toString(false).equals(id)) {
				Triple typeMatch = Triple.createMatch(t.getSubject(), typeProperty.asNode(), exceptionTypeProperty.asNode());
				ExtendedIterator<Triple> typeTripleIter = model.getGraph().find(typeMatch);
				if (typeTripleIter.hasNext()) {
					return t.getSubject();
				}
			}
		}
		return null;
	}

	/**
	 * @return the id
	 */
	public String getLicenseExceptionId() {
		return licenseExceptionId;
	}

	/**
	 * @param id the id to set
	 * @throws InvalidSPDXAnalysisException 
	 */
	public void setLicenseExceptionId(String id) throws InvalidSPDXAnalysisException {
		if (model != null) {
			Node duplicateNode = findException(model, this.licenseExceptionId);
			if (duplicateNode != null && !duplicateNode.equals(this.exceptionNode)) {
				throw(new InvalidSPDXAnalysisException("Can not set the License Exception ID to "+id+".  That ID is already in use."));
			}
		}
		this.licenseExceptionId = id;
		if (this.exceptionNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION_ID);
			model.removeAll(resource, p, null);
			// add the property
			if (id != null) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_LICENSE_EXCEPTION_ID);
				resource.addProperty(p, id);
			}
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		if (this.exceptionNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_NAME);
			model.removeAll(resource, p, null);
			// add the property
			if (name != null) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_NAME);
				resource.addProperty(p, name);
			}
		}
	}

	/**
	 * @return the text
	 */
	public String getLicenseExceptionText() {
		return licenseExceptionText;
	}

	/**
	 * @param text the text to set
	 */
	public void setLicenseExceptionText(String text) {
		this.licenseExceptionText = text;
		if (this.exceptionNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXCEPTION_TEXT);
			model.removeAll(resource, p, null);
			// add the property
			if (text != null) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXCEPTION_TEXT);
				resource.addProperty(p, text);
			}
		}
	}

	/**
	 * @return the sourceUrl
	 */
	public String[] getSeeAlso() {
		return seeAlso;
	}

	/**
	 * @param url the sourceUrl to set
	 */
	public void setSeeAlso(String[] url) {
		this.seeAlso = url;
		if (this.exceptionNode != null) {
			Property seeAlsoPropery = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, 
					SpdxRdfConstants.RDFS_PROP_SEE_ALSO);
			this.resource.removeAll(seeAlsoPropery);
			if (url != null) {
				for (int i = 0; i < url.length; i++) {
					this.resource.addProperty(seeAlsoPropery, url[i]);
				}
			}
		}
	}

	/**
	 * @return
	 */
	public String getComment() {
		return this.comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
		if (this.exceptionNode != null) {
			Property commentProperty = model.createProperty(SpdxRdfConstants.RDFS_NAMESPACE, 
					SpdxRdfConstants.RDFS_PROP_COMMENT);
			model.removeAll(this.resource, commentProperty, null);
			if (comment != null) {
				this.resource.addProperty(commentProperty, comment);
			}
		}
	}

	/**
	 * @return
	 */
	public String getExample() {
		return example;
	}
	
	public void setExample(String example) {
		this.example = example;
		if (this.exceptionNode != null) {
			// delete any previous created
			Property p = model.getProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXAMPLE);
			model.removeAll(resource, p, null);
			// add the property
			if (example != null) {
				p = model.createProperty(SpdxRdfConstants.SPDX_NAMESPACE, SpdxRdfConstants.PROP_EXAMPLE);
				resource.addProperty(p, example);
			}
		}
	}
	
	public LicenseException clone() {
		return new LicenseException(this.getLicenseExceptionId(), this.getName(), this.getLicenseExceptionText(),
				this.seeAlso, this.comment, this.example);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#toString()
	 */
	@Override
	public String toString() {
		return "WITH "+this.licenseExceptionId;
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LicenseException)) {
			return false;
		}
		LicenseException le = (LicenseException)o;
		if (this.licenseExceptionId == null || le.licenseExceptionId == null) {
			return false;
		}
		return this.licenseExceptionId.equals(le.licenseExceptionId);
	}

	/* (non-Javadoc)
	 * @see org.spdx.rdfparser.license.AnyLicenseInfo#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.licenseExceptionId == null) {
			return 0;
		} else {
			return this.licenseExceptionId.hashCode();
		}
	}

	public ArrayList<String> verify() {
		ArrayList<String> retval = new ArrayList<String>();
		if (this.getLicenseExceptionId() == null || this.getLicenseExceptionId().trim().isEmpty()) {
			retval.add("Missing required license exception ID");
		}
		if (this.getLicenseExceptionText() == null || this.getLicenseExceptionText().trim().isEmpty()) {
			retval.add("Missing required license exception text");
		}
		return retval;
	}
}
