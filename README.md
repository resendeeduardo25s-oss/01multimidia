# Minha Multimídia 2.0 — Premium Sport

Projeto Android original em Kotlin + Jetpack Compose, preparado para centrais horizontais Android.

## Direção visual
Identidade premium esportiva: preto/grafite, contraste alto e acentos azul, vermelho, âmbar e neon rosa.

## Veículos
Golf, Argo, Polo, Jetta, Tiggo e Palio — usando as seis imagens enviadas pelo usuário.

## Funcionalidades implementadas nesta base 2.0
- Animação de boas-vindas com o nome do veículo atualmente selecionado.
- Garagem com os seis modelos.
- Paleta de oito cores de pintura (filtro visual aplicado à imagem inteira nesta etapa).
- Cinco temas selecionáveis: Simbionte, Simbionte Carnificina, Garagem Âmbar, Costa Neon e NFS Underground Garage (background enviado pelo usuário).
- Seleção de imagem de fundo própria com permissão persistente e gravação da escolha.
- Painel de configurações com opções para mostrar/ocultar relógio, velocidade, bateria OBD2, atalhos, nome do carro, animação e controle visual dos faróis.
- Velocidade por GPS, com solicitação de permissão.
- Botão de faróis com efeito visual na interface.
- Atalhos para Google Maps e aplicativo de música.

## Referências funcionais analisadas
O APK de referência fornecido pelo usuário foi inspecionado para mapear funcionalidades e fluxos: animação do veículo, Drive Lab, telemetria OBD2, medidores configuráveis, alerta de temperatura, conexão Bluetooth clássico/BLE e alternância GPS/OBD2. Esta base implementa a interface e o GPS; os recursos de telemetria e alertas serão desenvolvidos como módulos próprios na próxima etapa. O código do APK de referência não foi copiado para este projeto.

## Limitações desta base
- Bateria, temperatura, RPM e consumo do veículo exigem conexão OBD2 compatível e implementação do protocolo; a tela mostra indisponibilidade em vez de inventar leituras.
- O controle dos faróis é um efeito visual; não aciona os faróis físicos do carro.
- A velocidade depende do GPS e pode ficar indisponível em ambientes fechados.
- A imagem de fundo personalizada é salva e restaurada. O tema NFS Underground Garage também está incluído como background nativo selecionável.
- A cor do carro é um filtro visual aplicado à imagem inteira. Para mudar somente a lataria sem afetar vidros, rodas, pneus e faróis, cada veículo precisa de uma máscara de pintura própria.
- Os modelos fornecidos têm recortes e proporções diferentes; o enquadramento deve ser validado na resolução real da central.

## Como abrir e gerar APK
1. Extraia o ZIP.
2. Abra a pasta `MinhaMultimidia_Atualizada` no Android Studio.
3. Aguarde o Gradle Sync.
4. Use JDK 17 e Android SDK 35.
5. Execute em um dispositivo Android ou selecione **Build > Build APK(s)**.
6. O APK de depuração ficará em `app/build/outputs/apk/debug/app-debug.apk`.

Este pacote contém código-fonte e recursos. Não é um APK compilado/testado em uma central real.


## Gerar o APK pelo GitHub Actions
O projeto inclui o workflow `.github/workflows/build-apk.yml`.

1. Crie um repositório no GitHub.
2. Envie todo o conteúdo desta pasta para a branch `main` (incluindo a pasta `.github`).
3. Abra a aba **Actions** do repositório.
4. Se necessário, habilite GitHub Actions para o repositório.
5. Aguarde o workflow **Gerar APK - Minha Multimídia** terminar.
6. Abra a execução concluída e baixe o artefato **multimidia-apk** na seção Artifacts.

O workflow também pode ser iniciado manualmente em **Actions > Gerar APK - Minha Multimídia > Run workflow**. O APK gerado é de depuração (`app-debug.apk`), adequado para instalação e testes; não é uma versão assinada para publicação na Play Store.


## Uso independente e retorno ao launcher original
- O app é instalado como um aplicativo Android comum e não altera ROM nem firmware.
- O Manifest declara a atividade como candidata a launcher (HOME). Isso não a torna padrão automaticamente: o Android só muda a tela inicial após escolha explícita do usuário.
- Em **Configurações do painel > Escolher tela inicial do Android**, o sistema abre as configurações de tela inicial para escolher o launcher original ou Minha Multimídia.
- Para remover, basta desinstalar o aplicativo nas configurações do Android.
- O nome e o caminho exato das opções do Android podem variar conforme o fabricante da central.
