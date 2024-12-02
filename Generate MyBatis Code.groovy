import com.intellij.database.model.DasTable
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Available context bindings:
 *   SELECTION   Iterable<DasObject>
 *   PROJECT     project
 *   FILES       files helper
 */

typeMapping = [
        (~/(?i)int/)                      : "Long",
        (~/(?i)float|double|decimal|real/): "BigDecimal",
        (~/(?i)datetime|timestamp/)       : "java.util.Date",
        (~/(?i)date/)                     : "java.sql.Date",
        (~/(?i)time/)                     : "java.sql.Time",
        (~/(?i)/)                         : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = javaName(table.getName().replaceFirst('t_', ''), true)
    def fields = calcFields(table)

    dirPath = dir.getAbsolutePath()
    packageName = calcPackageName(dirPath)

    // ensure directory exists
    new File(dirPath + File.separator + "entity").mkdirs()
    new File(dirPath + File.separator + "mapper").mkdirs()
    new File(dirPath + File.separator + "service").mkdirs()
    new File(dirPath + File.separator + "service" + File.separator + "impl").mkdirs()

    // Generate POJO
    new File(dirPath + File.separator + "entity", className + ".java").withPrintWriter("utf-8") { out -> generateEntity(out, table.getName(), className, fields, packageName) }

    // Generate Mapper
    new File(dirPath + File.separator + "mapper", className + "Mapper.java").withPrintWriter("utf-8") { out -> generateMapper(out, className, packageName) }

    // Generate Service
    new File(dirPath + File.separator + "service", className + "Service.java").withPrintWriter("utf-8") { out -> generateService(out, className, packageName) }

    // Generate ServiceImpl
    new File(dirPath + File.separator + "service" + File.separator + "impl", className + "ServiceImpl.java").withPrintWriter("utf-8") { out -> generateServiceImpl(out, className, packageName) }
}

static def generateEntity(out, tableName, className, fields, packageName) {
    out.println "package $packageName" + ".entity;"
    out.println ""
    out.println "import com.test.common.base.BaseEntity;"
    out.println "import lombok.Data;"
    out.println "import lombok.EqualsAndHashCode;"
    out.println "import javax.persistence.Table;"
    out.println ""
    out.println "/**\n * @author mazhuang\n */"
    out.println "@EqualsAndHashCode(callSuper = true)"
    out.println "@Data"
    out.println "@Table(name = \"$tableName\")"
    out.println "public class $className extends BaseEntity {"
    out.println ""

    def baseEntityFields = ['pkid', 'addedBy', 'addedTime', 'lastModifiedBy', 'lastModifiedTime', 'valid']
    fields.each() {
        if (baseEntityFields.contains(it.name)) {
            return
        }
        if (it.annos != "") out.println "  ${it.annos}"
        if (it.comment != null) out.println "    /**\n     * ${it.comment}\n     */"
        out.println "    private ${it.type} ${it.name};\n"
    }

    out.println "}"
}

static def generateMapper(out, className, packageName) {
    out.println "package $packageName" + ".mapper;"
    out.println ""

    out.println "import com.test.common.base.BaseMapper;"
    out.println "import $packageName" + ".entity.$className;"
    out.println ""

    out.println "/**\n * @author mazhuang\n */"
    out.println "public interface $className" + "Mapper extends BaseMapper<$className> {"
    out.println ""
    out.println "}"
}

static def generateService(out, className, packageName) {
    out.println "package $packageName" + ".service;"
    out.println ""

    out.println "import com.test.common.base.BaseService;"
    out.println "import $packageName" + ".entity.$className;"
    out.println ""

    out.println "/**\n * @author mazhuang\n */"
    out.println "public interface $className" + "Service extends BaseService<$className> {"
    out.println ""
    out.println "}"
}

static def generateServiceImpl(out, className, packageName) {
    out.println "package $packageName" + ".service.impl;"
    out.println ""

    out.println "import lombok.extern.slf4j.Slf4j;"
    out.println "import org.springframework.stereotype.Service;"
    out.println "import com.test.common.base.BaseServiceImpl;"
    out.println "import $packageName" + ".entity.$className;"
    out.println "import $packageName" + ".mapper.$className" + "Mapper;"
    out.println "import $packageName" + ".service.$className" + "Service;"
    out.println ""

    out.println "/**\n * @author mazhuang\n */"
    out.println "@Slf4j"
    out.println "@Service"
    out.println "public class $className" + "ServiceImpl extends BaseServiceImpl<$className" + "Mapper, $className> implements $className" + "Service {"
    out.println ""
    out.println "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) {
        fields, col ->
            def spec = Case.LOWER.apply(col.getDataType().getSpecification())
            def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
            fields += [[
                               name   : javaName(col.getName(), false),
                               type   : typeStr,
                               comment: col.getComment(), // 注释
                               default: col.getDefault(), // 默认值
                               annos  : ""]]

    }
}

static def calcPackageName(dirPath) {
    def startPos = dirPath.indexOf('com')
    return dirPath.substring(startPos).replaceAll(File.separator, ".")
}

def javaName(str, capitalize) {
    def s = com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
            .replaceAll(/[^\p{javaJavaIdentifierPart}[_]]/, "_")
    capitalize || s.length() == 1 ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}
