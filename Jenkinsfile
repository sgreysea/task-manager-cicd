/*
 * Единый Jenkinsfile для двух лабораторных работ (проект task-manager-cicd).
 *
 * Часть 1, задание № 12 — параметризованный запуск с выбором набора проверок:
 *   TEST_MODE = FULL      -> mvn clean verify (unit + integration + JaCoCo + JAR)
 *   TEST_MODE = UNIT_ONLY -> mvn clean test   (только модульные тесты)
 *
 * Часть 2, задание № 12 — проверка Pull Request с минимальными правами.
 * Сначала отлаживается здесь, в Jenkins, затем переносится в GitHub Actions
 * (.github/workflows/pr-check.yml):
 *   RUN_TYPE = PR_CHECK   -> только mvn clean verify, режим TEST_MODE игнорируется,
 *                            JAR не архивируется и не фиксируется (fingerprint),
 *                            учётные данные и публикация не используются (только чтение).
 *   RUN_TYPE = BRANCH_BUILD -> обычная сборка ветки (аналог ci.yml).
 *
 * Соответствие Jenkins -> GitHub Actions:
 *   RUN_TYPE=BRANCH_BUILD  -> ci.yml (push, workflow_dispatch с test_mode)
 *   RUN_TYPE=PR_CHECK      -> pr-check.yml (pull_request, permissions: contents: read)
 *   TEST_MODE              -> inputs.test_mode
 *   stage Build and Verify -> step "Build and verify"
 *   junit / archiveArtifacts -> actions/upload-artifact
 */
pipeline {

    agent {
        label 'windows-java21-agent'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 30, unit: 'MINUTES')
    }

    parameters {
        choice(
            name: 'RUN_TYPE',
            choices: ['BRANCH_BUILD', 'PR_CHECK'],
            description: 'BRANCH_BUILD — сборка ветки; PR_CHECK — проверка Pull Request (только чтение, без JAR)'
        )
        choice(
            name: 'TEST_MODE',
            choices: ['FULL', 'UNIT_ONLY'],
            description: 'FULL — unit + integration + JAR; UNIT_ONLY — только модульные тесты (для PR_CHECK игнорируется)'
        )
    }

    environment {
        // PR_CHECK всегда выполняет полную проверку, как pr-check.yml
        EFFECTIVE_MODE = "${params.RUN_TYPE == 'PR_CHECK' ? 'FULL' : params.TEST_MODE}"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Тип запуска: ${params.RUN_TYPE}; режим проверки: ${env.EFFECTIVE_MODE}"
                script {
                    currentBuild.description = "${params.RUN_TYPE} / ${env.EFFECTIVE_MODE}"
                }
                checkout scm
            }
        }

        stage('Toolchain') {
            steps {
                bat 'java -version'
                bat 'mvn -version'
                bat 'git --version'
            }
        }

        stage('Build and Verify (FULL)') {
            when {
                expression { env.EFFECTIVE_MODE == 'FULL' }
            }
            steps {
                echo 'Полная проверка: mvn clean verify'
                bat 'mvn -B -ntp clean verify'
            }
        }

        stage('Unit Tests (UNIT_ONLY)') {
            when {
                expression { env.EFFECTIVE_MODE == 'UNIT_ONLY' }
            }
            steps {
                echo 'Сокращённая проверка: mvn clean test'
                bat 'mvn -B -ntp clean test'
            }
        }

        stage('Generate Reports') {
            steps {
                echo 'Формирование HTML-отчёта Surefire'
                bat 'mvn -B -ntp surefire-report:report-only'
                echo 'Формирование HTML-отчёта JaCoCo'
                bat 'mvn -B -ntp jacoco:report'
            }
        }
    }

    post {

        always {
            junit(
                allowEmptyResults: true,
                testResults: 'target/surefire-reports/TEST-*.xml,target/failsafe-reports/TEST-*.xml'
            )
            archiveArtifacts(
                artifacts: 'target/surefire-reports/**,target/failsafe-reports/**,target/site/**',
                allowEmptyArchive: true
            )
        }

        // JAR сохраняется только для сборки ветки в режиме FULL.
        // Для PR_CHECK результат — только отчёты (как в pr-check.yml).
        success {
            script {
                if (params.RUN_TYPE == 'BRANCH_BUILD' && env.EFFECTIVE_MODE == 'FULL') {
                    archiveArtifacts(
                        artifacts: 'target/task-manager-cicd-1.0.0.jar',
                        fingerprint: true
                    )
                } else {
                    echo 'JAR не архивируется (PR_CHECK или UNIT_ONLY).'
                }
            }
        }

        failure {
            echo 'Pipeline завершился с ошибкой. Изучите Console Output и отчёты.'
        }

        unstable {
            echo 'Pipeline имеет статус UNSTABLE. Проверьте результаты тестирования.'
        }
    }
}
