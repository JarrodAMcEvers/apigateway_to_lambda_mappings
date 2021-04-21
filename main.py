import boto3
import json
methods = ['ANY', 'GET', 'POST', 'DELETE', 'PUT', 'PATCH']

data = dict()
apigateway = boto3.client('apigateway')
apis = apigateway.get_rest_apis(limit=100)

for api in apis['items']:
    api_data = {}
    results = apigateway.get_resources(restApiId=api['id'], limit=777)
    for result in results['items']:
        for method in methods:
            try:
                integration = apigateway.get_integration(restApiId=api['id'], resourceId=result['id'], httpMethod=method)
                if 'lambda' not in integration['uri']:
                    continue
                lambda_function = integration['uri'].split(':')[-1].split('/')[0]
                if lambda_function not in api_data:
                    api_data[lambda_function] = []
                api_data[lambda_function].append('%s %s' % (method, result['path']))
            except apigateway.exceptions.NotFoundException as e:
                continue
    data[api['name']] = api_data

with open('apigateway_lambda.json', 'w') as outfile:
    json.dump(data, outfile)
