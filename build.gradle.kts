// 모든 서브프로젝트·모듈에 공통으로 적용할 설정을 두는 최상위 빌드 파일.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.roborazzi) apply false
}

// README의 모듈 의존성 트리를 실제 의존성으로 다시 생성한다 — `./gradlew updateModuleGraph`
// main 에 머지되면 .github/workflows/module-graph.yml 이 실행하고 변경분을 커밋한다.
tasks.register("updateModuleGraph") {
    val readme = layout.projectDirectory.file("README.md").asFile
    // main 코드의 모듈 의존성(api·implementation)만 수집한다. test 계열 configuration은 제외한다.
    val deps: Map<String, List<String>> = subprojects.associate { module ->
        module.path to listOf("api", "implementation")
            .mapNotNull { module.configurations.findByName(it) }
            .flatMap { it.dependencies.withType<ProjectDependency>().map { dep -> dep.path } }
            .distinct()
    }
    inputs.property("deps", deps)
    outputs.file(readme)

    doLast {
        // :app 에서 출발한다 — :app 을 instrument 하는 테스트 전용 :app-e2e 는 트리에 포함되지 않는다
        val lines = mutableListOf(":app")
        val expanded = mutableSetOf<String>()
        fun draw(path: String, indent: String) {
            val children = deps[path].orEmpty()
            children.forEachIndexed { i, dep ->
                val last = i == children.lastIndex
                val repeated = dep in expanded && deps[dep].orEmpty().isNotEmpty()
                lines += indent + (if (last) "└── " else "├── ") + dep + if (repeated) " (*)" else ""
                if (!repeated) {
                    expanded += dep
                    draw(dep, indent + if (last) "    " else "│   ")
                }
            }
        }
        draw(":app", "")

        val start = "<!-- module-graph:start -->"
        val end = "<!-- module-graph:end -->"
        val text = readme.readText()
        check(start in text && end in text) { "README.md 에 $start / $end 마커가 없습니다" }
        val block = lines.joinToString("\n", prefix = "$start\n```\n", postfix = "\n```\n$end")
        readme.writeText(text.substringBefore(start) + block + text.substringAfter(end))
    }
}
