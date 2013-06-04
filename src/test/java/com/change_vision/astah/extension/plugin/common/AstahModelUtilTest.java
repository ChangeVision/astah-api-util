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
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

public class AstahModelUtilTest {
	private ProjectAccessor projectAccessor;
	private AstahModelUtil util;

	@Before
	public void before() throws Throwable {
		util = new AstahModelUtil();

		this.projectAccessor = AstahAPI.getAstahAPI().getProjectAccessor();
		this.projectAccessor.open(this.getClass().getClassLoader().getResourceAsStream("testcase01.asta"));
	}

	@After
	public void after() throws Exception {
		this.projectAccessor.close();
	}

	@Test
	public void パッケージ全取得() throws Exception {
		System.out.println("ModelUtilTest.パッケージ全取得()");
		List<IPackage> packages = util.getPackages(this.projectAccessor.getProject());
		assertThat(5, is(packages.size()));
		assertThat("no title", is(packages.get(0).toString()));
		assertThat(true, is(util.isProjectPackage(packages.get(0))));
		assertThat("foo", is(packages.get(1).toString()));
		assertThat(false, is(util.isProjectPackage(packages.get(1))));
		assertThat("impl", is(packages.get(2).toString()));
		assertThat(false, is(util.isProjectPackage(packages.get(2))));
	}

	@Test
	public void クラス全取得() throws Exception {
		System.out.println("ModelUtilTest.クラス全取得()");
		List<IClass> classes = util.getClasses(this.projectAccessor.getProject());
		Collections.sort(classes, new Comparator<IClass>() {
			@Override
			public int compare(IClass o1, IClass o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		assertThat(6, is(classes.size()));
		assertThat("Bar", is(classes.get(0).getName()));
		assertThat("foo.Bar", is(classes.get(0).getFullName(".")));
		assertThat("Bar2", is(classes.get(1).getName()));
		assertThat("foo2.Bar2", is(classes.get(1).getFullName(".")));
		assertThat("Bar2Impl", is(classes.get(2).getName()));
		assertThat("foo2.impl.Bar2Impl", is(classes.get(2).getFullName(".")));
		assertThat("BarImpl", is(classes.get(3).getName()));
		assertThat("foo.impl.BarImpl", is(classes.get(3).getFullName(".")));
		assertThat("Bee", is(classes.get(4).getName()));
		assertThat("foo.impl.BarImpl.Bee", is(classes.get(4).getFullName(".")));
		assertThat("Boo", is(classes.get(5).getName()));
		assertThat("Boo", is(classes.get(5).getFullName(".")));
	}

	@Test
	public void クラスのパッケージを全取得() throws Exception {
		System.out.println("ModelUtilTest.クラスのパッケージを全取得()");
		List<IClass> classes = util.getClasses(this.projectAccessor.getProject());
		Collections.sort(classes, new Comparator<IClass>() {
			@Override
			public int compare(IClass o1, IClass o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		assertThat(6, is(classes.size()));
		assertThat("foo", is(classes.get(0).getFullNamespace("/")));
		assertThat("foo2", is(classes.get(1).getFullNamespace("/")));
		assertThat("foo2/impl", is(classes.get(2).getFullNamespace("/")));
		assertThat("foo/impl", is(classes.get(3).getFullNamespace("/")));
		assertThat("foo/impl/BarImpl", is(classes.get(4).getFullNamespace("/")));
		assertThat("", is(classes.get(5).getFullNamespace("/")));
	}

	@Test
	public void 子クラスを名前で取得する() throws Exception {
		System.out.println("AstahModelUtilTest.子クラスを名前で取得する()");
		{
			IPackage childModel = util.getChildModel(IPackage.class, this.projectAccessor.getProject(), "foo", true);
			assertThat(childModel.getName(), is("foo"));
		}
		{
			IPackage childModel = util.getChildModel(IPackage.class, this.projectAccessor.getProject(), "foo2", true);
			assertThat(childModel.getName(), is("foo2"));
		}
		{
			IClass childModel = util.getChildModel(IClass.class, this.projectAccessor.getProject(), "Boo", true);
			assertThat(childModel.getName(), is("Boo"));
		}
	}

	@Test
	public void パスからモデルを取得() throws Exception {
		System.out.println("AstahModelUtilTest.パスからモデルを取得()");
		IModel model = this.projectAccessor.getProject();
		for (int i = 0; i < 2; ++i) {
			{
				IPackage pkg = util.getModelWithPath(IPackage.class, model, "foo");
				assertThat(pkg, is(not(nullValue())));
				assertThat(pkg.getFullName("/"), is("foo"));
			}
			{
				IPackage pkg = util.getModelWithPath(IPackage.class, model, "foo2");
				assertThat(pkg, is(not(nullValue())));
				assertThat(pkg.getFullName("/"), is("foo2"));
			}
			{
				IPackage pkg = util.getModelWithPath(IPackage.class, model, "foo2::impl");
				assertThat(pkg, is(not(nullValue())));
				assertThat(pkg.getFullName("/"), is("foo2/impl"));
			}
			{
				IPackage pkg = util.getModelWithPath(IPackage.class, model, "foo::impl");
				assertThat(pkg, is(not(nullValue())));
				assertThat(pkg.getFullName("/"), is("foo/impl"));
			}
			{
				IClass clazz = util.getModelWithPath(IClass.class, model, "foo::impl::BarImpl");
				assertThat(clazz, is(not(nullValue())));
				assertThat(clazz.getFullName("/"), is("foo/impl/BarImpl"));
			}
			{
				IClass clazz = util.getModelWithPath(IClass.class, model, "foo::Bar");
				assertThat(clazz, is(not(nullValue())));
				assertThat(clazz.getFullName("/"), is("foo/Bar"));
			}
			{
				IClass clazz = util.getModelWithPath(IClass.class, model, "foo::impl::BarImpl::Bee");
				assertThat(clazz, is(not(nullValue())));
				assertThat(clazz.getFullName("/"), is("foo/impl/BarImpl/Bee"));
			}
			{
				IPackage pkg = util.getModelWithPath(IPackage.class, model, "foo::impl3");
				assertThat(pkg, is(nullValue()));
			}
			{
				IClass clazz = util.getModelWithPath(IClass.class, model, "foo::impl::BarImpl::Bee3");
				assertThat(clazz, is(nullValue()));
			}
		}
	}

	@Test
	public void getParentNamespace_テスト() throws Throwable {
		{
			NamespaceClass nsc = util.getParentNamespace("");
			assertThat(nsc.namespace, is(""));
			assertThat(nsc.clazz, is(""));
		}
		{
			NamespaceClass nsc = util.getParentNamespace("a");
			assertThat(nsc.namespace, is(""));
			assertThat(nsc.clazz, is("a"));
		}
		{
			NamespaceClass nsc = util.getParentNamespace("a::b");
			assertThat(nsc.namespace, is("a"));
			assertThat(nsc.clazz, is("b"));
		}
		{
			NamespaceClass nsc = util.getParentNamespace("a::b::c");
			assertThat(nsc.namespace, is("a::b"));
			assertThat(nsc.clazz, is("c"));
		}
		{
			NamespaceClass nsc = util.getParentNamespace("a::b::c::d");
			assertThat(nsc.namespace, is("a::b::c"));
			assertThat(nsc.clazz, is("d"));
		}
	}

	@Test
	public void clearNamespace_test() {
		assertThat(util.clearNamespace("cv::astah::Hoge"), is("Hoge"));
		assertThat(util.clearNamespace("std::vector<std::string>"), is("vector<string>"));
		assertThat(util.clearNamespace("std::vector<std::vector<std::string>>"), is("vector<vector<string>>"));
		assertThat(util.clearNamespace("std::map<std::string, string>"), is("map<string,string>"));
		assertThat(util.clearNamespace("std::map<std::string, std::vector<std::string>>"), is("map<string,vector<string>>"));
		assertThat(util.clearNamespace("std::map<std::vector<std::string>, std::string>"), is("map<vector<string>,string>"));
	}
}
