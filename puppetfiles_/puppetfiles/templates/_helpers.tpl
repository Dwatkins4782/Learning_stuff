{{/* Standard Tracking Annotations */}}
{{- define "puppetfiles.tracking.annotation" }}
ercot.com/scm: {{ .Values.application.scm | quote }}
{{- end }}

{{/* Shared Basic Labels */}}
{{- define "puppetfiles.labels" }}
helm.sh/chart: "{{ .Chart.Name }}-{{ .Chart.Version }}"
app.kubernetes.io/name: {{ .Values.application.name | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}

{{/* Shared Basic Selector */}}
{{- define "puppetfiles.selector" }}
app.kubernetes.io/name: {{ .Values.application.name | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service | quote }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- end }}

{{/* puppetfiles Service Labels */}}
{{- define "puppetfiles.labels.puppetfiles" }}
{{- include "puppetfiles.labels" . }}
app.kubernetes.io/component: {{ .Values.puppetfiles.component | quote }}
{{- end }}

{{/* puppetfiles Service Selector */}}
{{- define "puppetfiles.selector.puppetfiles" }}
{{- include "puppetfiles.selector" . }}
app.kubernetes.io/component: {{ .Values.puppetfiles.component | quote }}
{{- end }}

{{/* Standard Prometheus Config */}}
{{- define "puppetfiles.metrics.annotation" }}
prometheus.io/scrape: "true"
prometheus.io/path: {{ .Values.metrics.path | quote }}
prometheus.io/port: {{ .Values.metrics.port | quote }}
{{- end }}

{{/* Standard Ingress Config */}}
{{- define "puppetfiles.ingress.annotation" }}
kubernetes.io/tls-acme: "true"
kubernetes.io/ingress.allow-http: "false"
{{- end }}

{{/* Deployment API Version */}}
{{- define "deployment.version" }}
apiVersion: "apps/v1"
{{- end }}

{{/* Disruption API Version */}}
{{- define "disruption.version" }}
apiVersion: "policy/v1beta1"
{{- end }}

{{/* HPA API Version */}}
{{- define "hpa.version" }}
apiVersion: "autoscaling/v2beta2"
{{- end }}

{{/* Config-Map API Version */}}
{{- define "config.version" }}
apiVersion: "v1"
{{- end }}

{{/* Secret API Version */}}
{{- define "secret.version" }}
apiVersion: "v1"
{{- end }}

{{/* Service API Version */}}
{{- define "service.version" }}
apiVersion: "v1"
{{- end }}

{{/* Ingress API Version */}}
{{- define "ingress.version" }}
apiVersion: "networking.k8s.io/v1"
{{- end }}

{{/* Route API Version */}}
{{- define "route.version" }}
apiVersion: "route.openshift.io/v1"
{{- end }}