package com.change_vision.astah.extension.plugin.common;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IAssociationClass;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IRequirement;
import com.change_vision.jude.api.inf.model.ISubsystem;
import com.change_vision.jude.api.inf.model.ITestCase;
import com.change_vision.jude.api.inf.model.IUseCase;

/**
 * astah上のモデルを扱うユーティリティ
 */
public class AstahModelUtil {
	private static final Logger LOG = LoggerFactory.getLogger(AstahModelUtil.class);
	private static final String DELIMITER = "::";

	/**
	 * 指定したパッケージより、すべてのパッケージを取得する
	 * 
	 * @param thePackage
	 *            起点のパッケージ
	 * @return パッケージのリスト
	 */
	public List<IPackage> getPackages(IPackage thePackage) {
		List<IPackage> packages = new ArrayList<IPackage>();
		packages.add(thePackage);
		return this.getPackages(thePackage, packages);
	}

	/**
	 * 指定パッケージ配下のパッケージを、再帰的に全て取得する。
	 * 
	 * @param thePackage
	 *            指定パッケージ
	 * @param packageList
	 *            パッケージ一覧を格納するリスト
	 * @return パッケージ一覧を格納したリスト
	 */
	public List<IPackage> getPackages(IPackage thePackage, List<IPackage> packageList) {
		INamedElement[] namedElements = thePackage.getOwnedElements();
		for (INamedElement namedElement : namedElements) {
			if (namedElement instanceof IPackage) {
				IPackage p = (IPackage) namedElement;
				packageList.add(p);
				getPackages(p, packageList);
			}
		}
		return packageList;
	}

	/**
	 * 指定パッケージ配下のクラスを、再帰的に全て取得する。
	 * 
	 * @param thePackage
	 *            指定パッケージ
	 * @return パッケージ一覧を格納したリスト
	 */
	public List<IClass> getClasses(IPackage thePackage) {
		return this.getClasses(thePackage, new ArrayList<IClass>());
	}

	/**
	 * 指定パッケージ配下のクラスを、再帰的に全て取得する。
	 * 
	 * @param thePackage
	 *            指定パッケージ
	 * @param classes
	 *            パッケージ一覧を格納するリスト
	 * @return パッケージ一覧を格納したリスト
	 */
	public List<IClass> getClasses(IPackage thePackage, List<IClass> classes) {
		if (thePackage == null) {
			return classes;
		}
		INamedElement[] namedElements = thePackage.getOwnedElements();
		for (INamedElement namedElement : namedElements) {
			if (namedElement instanceof IClass
					&& !(namedElement instanceof IUseCase || namedElement instanceof ITestCase || namedElement instanceof ISubsystem
							|| namedElement instanceof IRequirement || namedElement instanceof IAssociationClass)) {
				IClass c = (IClass) namedElement;
				classes.add(c);
				getNestedClass(c.getNestedClasses(), classes);
			}
			if (namedElement instanceof IPackage) {
				IPackage p = (IPackage) namedElement;
				getClasses(p, classes);
			}
		}
		return classes;
	}

	private List<IClass> getNestedClass(IClass[] nestedClasses, List<IClass> classes) {
		if (nestedClasses == null || nestedClasses.length <= 0) {
			return classes;
		}
		for (IClass clazz : nestedClasses) {
			classes.add(clazz);
			getNestedClass(clazz.getNestedClasses(), classes);
		}
		return classes;
	}

	/**
	 * プロジェクトパッケージか判断する
	 * 
	 * @param thePackage
	 * @return
	 */
	public boolean isProjectPackage(INamedElement thePackage) {
		if (thePackage.getOwner() == null) {
			return true;
		}
		return false;
	}

	protected INamedElement[] getChildrenElement(INamedElement el) {
		INamedElement[] ownedElements = new INamedElement[0];
		if (el instanceof IPackage) {
			ownedElements = ((IPackage) el).getOwnedElements();
		} else if (el instanceof IClass) {
			ownedElements = ((IClass) el).getNestedClasses();
		}
		return ownedElements;
	}

	/**
	 * 子クラスを名前で取得する
	 * 
	 * @param clazz
	 * @param el
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <M extends INamedElement> M getChildModel(Class<M> clazz, INamedElement el, String name, boolean validateInstanceOf) {
		INamedElement[] ownedElements = getChildrenElement(el);
		for (INamedElement namedElement : ownedElements) {
			if (name.equals(namedElement.getName())) {
				if (validateInstanceOf) {
					if (clazz.isInstance(namedElement)) {
						return (M) namedElement;
					} else {
						return null;
					}
				}
				return (M) namedElement;
			}
		}
		return null;
	}

	private Map<String, INamedElement> cache = new HashMap<String, INamedElement>();

	/**
	 * パスからモデルを取得する
	 * 
	 * @param clazz
	 *            取得したいクラス IClass や IPakcage など
	 * @param project
	 *            rootモデルを指定する
	 * @param namespace
	 *            java::lang::String 等、「::」で指定する
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public <M extends INamedElement> M getModelWithPath(Class<M> clazz, INamedElement project, String namespace) {
		String key = format("%s.%s", clazz.getSimpleName(), namespace);
		INamedElement obj = this.cache.get(key);
		if (obj != null) {
			LOG.trace(format("hit cache! : %s", key));
			return (M) obj;
		}

		String[] namespaces = namespace.split("::");
		INamedElement parentModel = project;
		int i = 0;
		while (i < namespaces.length) {
			String ns = namespaces[i];
			M childModel = getChildModel(clazz, parentModel, ns, i == namespaces.length - 1);
			if (childModel != null) {
				++i;
				parentModel = childModel;
				continue;
			}
			return null;
		}
		this.cache.put(key, parentModel);
		return (M) parentModel;
	}

	/**
	 * キャッシュをクリアする
	 */
	public void clear() {
		cache.clear();
	}

	public INamedElement getParentModel(IModel project, String namespace) throws InvalidEditingException {
		if ("".equals(namespace)) {
			return project;
		}
		INamedElement parentModel = this.getModelWithPath(IPackage.class, project, namespace);
		if (parentModel == null) {
			parentModel = this.getModelWithPath(IClass.class, project, namespace);
		}
		return parentModel;
	}

	public NamespaceClass getParentNamespace(String namespaces) {
		String[] nss = namespaces.split(DELIMITER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nss.length - 1; ++i) {
			sb.append(DELIMITER);
			sb.append(nss[i]);
		}
		NamespaceClass nsc = new NamespaceClass();
		if (nss.length <= 1) {
			nsc.clazz = namespaces;
		} else {
			nsc.clazz = nss[nss.length - 1];
		}
		if (sb.length() > 0) {
			nsc.namespace = sb.toString().substring(DELIMITER.length());
		}
		return nsc;
	}

	private static final String TEMPLATE_BEGIN = "<";
	private static final String TEMPLATE_END = ">";
	private static final String TEMPLATE_COMMA = ",";

	public String clearNamespace(String str) {
		StringBuilder sb = new StringBuilder();
		try {
			boolean appendBeginKakko = false;
			int index = 0;
			while (true) {
				int beginIndex = str.indexOf(TEMPLATE_BEGIN, index);
				int endIndex = str.indexOf(TEMPLATE_END, index);
				int commaIndex = str.indexOf(TEMPLATE_COMMA, index);
				if (beginIndex < 0 && commaIndex < 0) {
					if (endIndex < 0) {
						endIndex = str.length();
					}
					NamespaceClass namespaceClass = this.getParentNamespace(str.substring(index, endIndex));
					if (appendBeginKakko) {
						sb.append(TEMPLATE_BEGIN);
					}
					sb.append(namespaceClass.clazz);
					if (endIndex != str.length()) {
						sb.append(str.substring(endIndex));
					}
					break;
				}
				if (appendBeginKakko) {
					sb.append(TEMPLATE_BEGIN);
				}
				appendBeginKakko = true;
				if (commaIndex >= 0 && commaIndex < beginIndex) {
					NamespaceClass namespaceClass = this.getParentNamespace(str.substring(index, commaIndex));
					sb.append(namespaceClass.clazz);
					sb.append(TEMPLATE_COMMA);
					index = commaIndex + 1;
					appendBeginKakko = false;
				} else if (commaIndex >= 0 && beginIndex < 0) {
					NamespaceClass namespaceClass = this.getParentNamespace(str.substring(index, commaIndex));
					sb.append(namespaceClass.clazz);
					sb.append(TEMPLATE_COMMA);
					index = commaIndex + 1;
					appendBeginKakko = false;
				} else {
					NamespaceClass namespaceClass = this.getParentNamespace(str.substring(index, beginIndex));
					sb.append(namespaceClass.clazz);
					index = beginIndex + 1;
				}
			}
			return sb.toString().replace(" ", "");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
