package com.change_vision.astah.extension.plugin.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

public class AstahModelUtil_cpp_Test {
	private ProjectAccessor projectAccessor;

	@Before
	public void before() throws Throwable {
		this.projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
		this.projectAccessor.open(this.getClass().getClassLoader().getResourceAsStream("test_cpp.asta"));
	}

	@After
	public void after() throws Exception {
		this.projectAccessor.close();
	}

	@Test
	public void パッケージ全取得() throws Exception {
		System.out.println("ModelUtilTest.パッケージ全取得()");
		AstahModelUtil modelUtil = new AstahModelUtil();
		List<IPackage> packages = modelUtil.getPackages(this.projectAccessor.getProject());
		for (IPackage pkg : packages) {
			System.out.println(pkg.getFullName("::"));
		}
	}

	@Test
	public void クラス全取得() throws Exception {
		System.out.println("ModelUtilTest.クラス全取得()");
		AstahModelUtil modelUtil = new AstahModelUtil();
		List<IClass> classes = modelUtil.getClasses(this.projectAccessor.getProject());
		Collections.sort(classes, new Comparator<IClass>() {
			@Override
			public int compare(IClass o1, IClass o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (IClass clazz : classes) {
			if (!clazz.getName().isEmpty()) {
				System.out.println(clazz.getFullName("::"));
			}
		}
	}

	@Test
	public void _test() throws Exception {
		assertThat(this.projectAccessor.findElements(IClass.class, "vector"), is(not(nullValue())));
	}
}
